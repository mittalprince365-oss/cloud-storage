package com.cloudstorage.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shares")
public class Share {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long fileId;          // kaunsi file share hui

    @Column(nullable = false)
    private Long ownerId;         // kisne share ki

    @Column(nullable = false)
    private Long sharedWithId;    // kis user ke saath

    @Column(nullable = false)
    private String role = "VIEWER";   // VIEWER ya EDITOR

    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Long getSharedWithId() { return sharedWithId; }
    public void setSharedWithId(Long sharedWithId) { this.sharedWithId = sharedWithId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}