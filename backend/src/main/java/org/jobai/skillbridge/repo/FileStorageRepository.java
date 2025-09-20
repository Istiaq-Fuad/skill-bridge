package org.jobai.skillbridge.repo;

import org.jobai.skillbridge.model.FileStorage;
import org.jobai.skillbridge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileStorageRepository extends JpaRepository<FileStorage, Long> {

    List<FileStorage> findByUserAndIsActive(User user, boolean isActive);

    List<FileStorage> findByUserAndCategoryAndIsActive(User user, FileStorage.FileCategory category, boolean isActive);

    Optional<FileStorage> findByFilePathAndIsActive(String filePath, boolean isActive);

    List<FileStorage> findByUserIdAndIsActive(Long userId, boolean isActive);

    Optional<FileStorage> findFirstByUserAndCategoryAndIsActiveOrderByUploadedAtDesc(User user,
            FileStorage.FileCategory category, boolean isActive);
}