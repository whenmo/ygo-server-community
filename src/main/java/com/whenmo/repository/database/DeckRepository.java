package com.whenmo.repository.database;

import com.whenmo.model.database.domain.Deck;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DeckRepository extends JpaRepository<Deck, Integer> {
    /**
     * 依據 deck_id 與 user_id 查詢單個卡組
     */
    Optional<Deck> findByIdAndUserId(Integer deckId, Integer userId);

    /**
     * 依據 deck_id 查詢公開卡組
     */
    @Query("SELECT d FROM Deck d WHERE d.id = :deckId AND d.isPublic = true")
    Optional<Deck> findPublicDeckById(Integer deckId);

    /**
     * 查詢所有公開卡組，依 updated_at 排序
     */
    @Query("SELECT d FROM Deck d WHERE d.isPublic = true ORDER BY d.updatedAt DESC")
    List<Deck> findPublicDecks();

    /**
     * 查詢指定 user_id 的所有卡組，依 is_favor 降冪排序，再依 updated_at 降冪排序
     */
    List<Deck> findByUserIdOrderByIsFavorDescUpdatedAtDesc(Integer userId);
}