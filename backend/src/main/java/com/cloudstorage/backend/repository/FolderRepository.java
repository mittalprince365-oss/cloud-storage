package com.cloudstorage.backend.repository;

import com.cloudstorage.backend.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByOwnerIdAndParentIdIsNullAndTrashedFalse(Long ownerId);
    List<Folder> findByOwnerIdAndParentIdAndTrashedFalse(Long ownerId, Long parentId);
    List<Folder> findByOwnerIdAndTrashedTrue(Long ownerId);
}