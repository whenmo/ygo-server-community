package com.whenmo.repository.database;

import com.whenmo.model.database.domain.Diyer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DiyerRepository extends JpaRepository<Diyer, Integer> {

    boolean existsByCdbIdAndName(Integer cdbId, String name);
    
    List<Diyer> findByCdbId(Integer cdbId);

    List<Diyer> findByUserId(Integer userId);
}