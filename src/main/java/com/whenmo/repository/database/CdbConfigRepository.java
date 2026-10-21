package com.whenmo.repository.database;

import org.springframework.data.jpa.repository.JpaRepository;

import com.whenmo.model.database.domain.CdbConfig;

import java.util.List;
import java.util.Optional;

public interface CdbConfigRepository extends JpaRepository<CdbConfig, Integer> {
    List<CdbConfig> findByNameContaining(String name);

    boolean existsByFilePath(String filePath);

    Optional<CdbConfig> findByFilePath(String filePath);
}