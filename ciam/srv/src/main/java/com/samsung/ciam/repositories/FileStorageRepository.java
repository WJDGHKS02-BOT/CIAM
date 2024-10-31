package com.samsung.ciam.repositories;

import com.samsung.ciam.models.FileStorage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileStorageRepository extends JpaRepository<FileStorage, Long> {
}