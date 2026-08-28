package com.cloudstorage.backend.controller;

import com.cloudstorage.backend.model.FileEntity;
import com.cloudstorage.backend.repository.FileRepository;
import com.cloudstorage.backend.security.JwtUtil;
import com.cloudstorage.backend.service.StorageService;
import com.cloudstorage.backend.repository.UserRepository;
import com.cloudstorage.backend.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    private final FileRepository fileRepository;
    private final StorageService storageService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public FileController(FileRepository fileRepository, StorageService storageService,
                          JwtUtil jwtUtil, UserRepository userRepository) {
        this.fileRepository = fileRepository;
        this.storageService = storageService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    // token se user id nikaalo (helper)
    private Long getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) return null;
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? user.getId() : null;
    }

    // ===== UPLOAD =====
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                    @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        try {
            // unique path banao: userId/timestamp_filename
                        String cleanName = file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
            String path = userId + "/" + System.currentTimeMillis() + "_" + cleanName;
            storageService.upload(file, path);

            // metadata database mein save
            FileEntity entity = new FileEntity();
            entity.setName(file.getOriginalFilename());
            entity.setStoragePath(path);
            entity.setFileType(file.getContentType());
            entity.setFileSize(file.getSize());
            entity.setOwnerId(userId);
            fileRepository.save(entity);

            return ResponseEntity.ok(entity);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    // ===== LIST MY FILES =====
    @GetMapping
    public ResponseEntity<?> myFiles(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        return ResponseEntity.ok(fileRepository.findByOwnerIdAndTrashedFalse(userId));
    }

    // ===== DOWNLOAD (public url) =====
    @GetMapping("/{id}/download")
    public ResponseEntity<?> download(@PathVariable Long id,
                                      @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        FileEntity file = fileRepository.findById(id).orElse(null);
        if (file == null || !file.getOwnerId().equals(userId)) {
            return ResponseEntity.status(404).body(Map.of("error", "File not found"));
        }
        String url = storageService.getPublicUrl(file.getStoragePath());
        return ResponseEntity.ok(Map.of("url", url, "name", file.getName()));
    }
}