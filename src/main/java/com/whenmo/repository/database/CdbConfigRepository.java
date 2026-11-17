package com.whenmo.repository.database;

import org.springframework.data.jpa.repository.JpaRepository;

import com.whenmo.model.database.domain.CdbConfig;

public interface CdbConfigRepository extends JpaRepository<CdbConfig, Integer> {

    boolean existsByName(String name);

    boolean existsByFilePath(String filePath);
}