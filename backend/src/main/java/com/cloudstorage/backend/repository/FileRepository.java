package com.cloudstorage.backend.repository;

import com.cloudstorage.backend.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    // us user ki files jo trash mein nahi hain
    List<FileEntity> findByOwnerIdAndTrashedFalse(Long ownerId);
    // us user ki trashed files
    List<FileEntity> findByOwnerIdAndTrashedTrue(Long ownerId);
}