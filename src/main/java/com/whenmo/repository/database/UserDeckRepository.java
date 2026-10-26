package com.whenmo.repository.database;

import com.whenmo.model.database.domain.UserDeck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserDeckRepository extends JpaRepository<UserDeck, Integer> {

    List<UserDeck> findByUserIdOrderByUpdatedAtDesc(Integer userId);

    List<UserDeck> findByIsPublicTrueOrderByUpdatedAtDesc();

    Optional<UserDeck> findByIdAndUserId(Integer id, Integer userId);

    boolean existsByUserIdAndNameIgnoreCase(Integer userId, String name);

    @Query("SELECT DISTINCT d.userId FROM UserDeck d")
    List<Integer> findAllDeckUserIds();
}