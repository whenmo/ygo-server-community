package com.whenmo.model.database.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.Formula;

@Data
@Entity
@Table(name = "decks")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false;

    @Column(name = "is_favor", nullable = false)
    @Builder.Default
    private Boolean isFavor = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "user_id", updatable = false)
    private Integer userId;

    @Formula("(SELECT u.name FROM users u WHERE u.id = user_id)")
    private String userName;

    @Column(name = "pack_id")
    private Integer packId;

    @Formula("(SELECT p.name FROM deck_packs p WHERE p.id = pack_id)")
    private String packName;

    @Formula("(SELECT CASE WHEN p.user_id IS NULL THEN 1 ELSE 0 END FROM deck_packs p WHERE p.id = pack_id)")
    private Boolean packIsPublic;

    @PrePersist
    void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}