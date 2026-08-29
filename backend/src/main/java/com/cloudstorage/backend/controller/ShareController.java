package com.cloudstorage.backend.controller;

import com.cloudstorage.backend.model.*;
import com.cloudstorage.backend.repository.*;
import com.cloudstorage.backend.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/shares")
@CrossOrigin(origins = "*")
public class ShareController {

    private final ShareRepository shareRepository;
    private final LinkShareRepository linkShareRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public ShareController(ShareRepository shareRepository, LinkShareRepository linkShareRepository,
                           FileRepository fileRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.shareRepository = shareRepository;
        this.linkShareRepository = linkShareRepository;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    private Long getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) return null;
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? user.getId() : null;
    }

    // ===== SHARE FILE WITH A USER (by email) =====
    @PostMapping
    public ResponseEntity<?> shareWithUser(@RequestBody Map<String, Object> body,
                                           @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        Long fileId = Long.valueOf(body.get("fileId").toString());
        String email = (String) body.get("email");
        String role = body.get("role") != null ? body.get("role").toString() : "VIEWER";

        // file meri hai?
        FileEntity file = fileRepository.findById(fileId).orElse(null);
        if (file == null || !file.getOwnerId().equals(userId)) {
            return ResponseEntity.status(404).body(Map.of("error", "File not found"));
        }

        // jisse share karna hai wo user exist karta hai?
        User target = userRepository.findByEmail(email).orElse(null);
        if (target == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No user with that email"));
        }
        if (target.getId().equals(userId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot share with yourself"));
        }

        Share share = new Share();
        share.setFileId(fileId);
        share.setOwnerId(userId);
        share.setSharedWithId(target.getId());
        share.setRole(role);
        shareRepository.save(share);

        return ResponseEntity.ok(Map.of("message", "File shared with " + email, "role", role));
    }

    // ===== SHARED WITH ME =====
    @GetMapping("/with-me")
    public ResponseEntity<?> sharedWithMe(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        List<Share> shares = shareRepository.findBySharedWithId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Share s : shares) {
            FileEntity file = fileRepository.findById(s.getFileId()).orElse(null);
            if (file != null && !file.isTrashed()) {
                Map<String, Object> m = new HashMap<>();
                m.put("shareId", s.getId());
                m.put("fileId", file.getId());
                m.put("fileName", file.getName());
                m.put("role", s.getRole());
                result.add(m);
            }
        }
        return ResponseEntity.ok(result);
    }

    // ===== CREATE PUBLIC LINK =====
    @PostMapping("/link")
    public ResponseEntity<?> createLink(@RequestBody Map<String, Object> body,
                                        @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        Long fileId = Long.valueOf(body.get("fileId").toString());
        FileEntity file = fileRepository.findById(fileId).orElse(null);
        if (file == null || !file.getOwnerId().equals(userId)) {
            return ResponseEntity.status(404).body(Map.of("error", "File not found"));
        }

        LinkShare link = new LinkShare();
        link.setToken(UUID.randomUUID().toString().replace("-", ""));
        link.setFileId(fileId);
        link.setOwnerId(userId);

        // expiry optional: body mein "expiryDays" ho to
        if (body.get("expiryDays") != null) {
            int days = Integer.parseInt(body.get("expiryDays").toString());
            link.setExpiresAt(LocalDateTime.now().plusDays(days));
        }
        linkShareRepository.save(link);

        return ResponseEntity.ok(Map.of(
            "token", link.getToken(),
            "shareUrl", "/api/shares/public/" + link.getToken(),
            "expiresAt", link.getExpiresAt() != null ? link.getExpiresAt().toString() : "never"
        ));
    }

    // ===== ACCESS PUBLIC LINK (no login) =====
    @GetMapping("/public/{token}")
    public ResponseEntity<?> accessLink(@PathVariable String token) {
        LinkShare link = linkShareRepository.findByToken(token).orElse(null);
        if (link == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Invalid link"));
        }
        // expiry check
        if (link.getExpiresAt() != null && link.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(410).body(Map.of("error", "This link has expired"));
        }
        FileEntity file = fileRepository.findById(link.getFileId()).orElse(null);
        if (file == null || file.isTrashed()) {
            return ResponseEntity.status(404).body(Map.of("error", "File not available"));
        }
        return ResponseEntity.ok(Map.of("fileName", file.getName(), "storagePath", file.getStoragePath()));
    }
}