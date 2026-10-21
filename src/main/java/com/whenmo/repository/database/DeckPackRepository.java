package com.whenmo.repository.database;

import com.whenmo.model.database.domain.DeckPack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeckPackRepository extends JpaRepository<DeckPack, String> {

    boolean existsByName(String name);
}