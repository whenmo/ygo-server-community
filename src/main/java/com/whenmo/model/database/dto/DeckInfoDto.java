package com.whenmo.model.database.dto;

import java.time.LocalDateTime;

import com.whenmo.model.database.domain.Deck;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class DeckInfoDto {
    private Integer id;
    private String name;
    private Boolean isPublic;
    private Boolean isFavor;
    private LocalDateTime updatedAt;

    public static DeckInfoDto fromDeck(Deck deck) {
        if (deck == null)
            return null;
        return new DeckInfoDto(
                deck.getId(),
                deck.getName(),
                deck.getIsPublic(),
                deck.getIsFavor(),
                deck.getUpdatedAt());
    }
}
