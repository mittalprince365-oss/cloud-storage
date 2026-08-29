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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    private Long getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) return null;
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? user.getId() : null;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                    @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            String cleanName = file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
            String path = userId + "/" + System.currentTimeMillis() + "_" + cleanName;
            storageService.upload(file, path);
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

    @GetMapping
    public ResponseEntity<?> myFiles(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        return ResponseEntity.ok(fileRepository.findByOwnerIdAndTrashedFalse(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(required = false, defaultValue = "") String query,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        Pageable pageable = PageRequest.of(page, size);
        Page<FileEntity> result;
        if (query.isBlank()) {
            result = fileRepository.findByOwnerIdAndTrashedFalse(userId, pageable);
        } else {
            result = fileRepository.findByOwnerIdAndTrashedFalseAndNameContainingIgnoreCase(userId, query, pageable);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("files", result.getContent());
        response.put("currentPage", result.getNumber());
        response.put("totalPages", result.getTotalPages());
        response.put("totalFiles", result.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trash")
    public ResponseEntity<?> trashList(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        return ResponseEntity.ok(fileRepository.findByOwnerIdAndTrashedTrue(userId));
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> trashFile(@PathVariable Long id,
                                       @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        FileEntity file = fileRepository.findById(id).orElse(null);
        if (file == null || !file.getOwnerId().equals(userId)) {
            return ResponseEntity.status(404).body(Map.of("error", "File not found"));
        }
        file.setTrashed(true);
        fileRepository.save(file);
        return ResponseEntity.ok(Map.of("message", "File moved to trash"));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<?> restoreFile(@PathVariable Long id,
                                         @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        FileEntity file = fileRepository.findById(id).orElse(null);
        if (file == null || !file.getOwnerId().equals(userId)) {
            return ResponseEntity.status(404).body(Map.of("error", "File not found"));
        }
        file.setTrashed(false);
        fileRepository.save(file);
        return ResponseEntity.ok(Map.of("message", "File restored"));
    }
}