package com.whenmo.repository.database;

import com.whenmo.model.database.domain.Diyer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiyerRepository extends JpaRepository<Diyer, Integer> {

    boolean existsByName(String name);

    boolean existsByUserId(Integer userId);

    Optional<Diyer> findByName(String name);

    Optional<Diyer> findByUserId(Integer userId);

    List<Diyer> findByNameContainingIgnoreCase(String name);
}