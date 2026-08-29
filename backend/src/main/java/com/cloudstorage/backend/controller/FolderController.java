package com.cloudstorage.backend.controller;

import com.cloudstorage.backend.model.Folder;
import com.cloudstorage.backend.model.User;
import com.cloudstorage.backend.repository.FolderRepository;
import com.cloudstorage.backend.repository.UserRepository;
import com.cloudstorage.backend.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/folders")
@CrossOrigin(origins = "*")
public class FolderController {

    private final FolderRepository folderRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public FolderController(FolderRepository folderRepository, JwtUtil jwtUtil, UserRepository userRepository) {
        this.folderRepository = folderRepository;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    private Long getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) return null;
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? user.getId() : null;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body,
                                    @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        String name = (String) body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Folder name required"));
        }
        Folder folder = new Folder();
        folder.setName(name);
        folder.setOwnerId(userId);
        if (body.get("parentId") != null) {
            folder.setParentId(Long.valueOf(body.get("parentId").toString()));
        }
        folderRepository.save(folder);
        return ResponseEntity.ok(folder);
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Long parentId,
                                  @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        List<Folder> folders;
        if (parentId == null) {
            folders = folderRepository.findByOwnerIdAndParentIdIsNullAndTrashedFalse(userId);
        } else {
            folders = folderRepository.findByOwnerIdAndParentIdAndTrashedFalse(userId, parentId);
        }
        return ResponseEntity.ok(folders);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> rename(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                    @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        Folder folder = folderRepository.findById(id).orElse(null);
        if (folder == null || !folder.getOwnerId().equals(userId)) {
            return ResponseEntity.status(404).body(Map.of("error", "Folder not found"));
        }
        String name = (String) body.get("name");
        if (name != null && !name.isBlank()) folder.setName(name);
        folderRepository.save(folder);
        return ResponseEntity.ok(folder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> trash(@PathVariable Long id,
                                   @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        Folder folder = folderRepository.findById(id).orElse(null);
        if (folder == null || !folder.getOwnerId().equals(userId)) {
            return ResponseEntity.status(404).body(Map.of("error", "Folder not found"));
        }
        folder.setTrashed(true);
        folderRepository.save(folder);
        return ResponseEntity.ok(Map.of("message", "Folder moved to trash"));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<?> restore(@PathVariable Long id,
                                     @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        Folder folder = folderRepository.findById(id).orElse(null);
        if (folder == null || !folder.getOwnerId().equals(userId)) {
            return ResponseEntity.status(404).body(Map.of("error", "Folder not found"));
        }
        folder.setTrashed(false);
        folderRepository.save(folder);
        return ResponseEntity.ok(Map.of("message", "Folder restored"));
    }
}