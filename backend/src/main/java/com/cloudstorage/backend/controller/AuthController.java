package com.cloudstorage.backend.controller;

import com.cloudstorage.backend.dto.LoginRequest;
import com.cloudstorage.backend.dto.RegisterRequest;
import com.cloudstorage.backend.model.User;
import com.cloudstorage.backend.repository.UserRepository;
import com.cloudstorage.backend.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // constructor - Spring khud ye teeno de dega
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ===== REGISTER =====
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.name == null || req.email == null || req.password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "All fields are required"));
        }
        if (req.password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters"));
        }
        if (userRepository.existsByEmail(req.email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }

        // user banao, password HASH karke
        User user = new User();
        user.setName(req.name);
        user.setEmail(req.email);
        user.setPassword(passwordEncoder.encode(req.password));
        userRepository.save(user);

        // JWT token banao
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", Map.of(
            "id", user.getId(),
            "name", user.getName(),
            "email", user.getEmail(),
            "role", user.getRole()
        ));
        return ResponseEntity.ok(response);
    }

    // ===== LOGIN =====
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        User user = userRepository.findByEmail(req.email).orElse(null);

        if (user == null || !passwordEncoder.matches(req.password, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid email or password"));
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", Map.of(
            "id", user.getId(),
            "name", user.getName(),
            "email", user.getEmail(),
            "role", user.getRole()
        ));
        return ResponseEntity.ok(response);
    }
}