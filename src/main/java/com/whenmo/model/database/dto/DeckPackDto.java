package com.whenmo.model.database.dto;

import com.whenmo.model.database.domain.Deck;
import com.whenmo.model.database.domain.DeckPack;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class DeckPackDto {
    private Integer id;
    private String name;
    private Boolean isPublic;

    public static DeckPackDto fromDeckPack(DeckPack deckPack) {
        if (deckPack == null)
            return null;

        return new DeckPackDto(deckPack.getId(), deckPack.getName(), deckPack.getIsPublic());
    }

    public static DeckPackDto fromDeck(Deck deck) {
        if (deck == null)
            return null;
        return new DeckPackDto(deck.getPackId(), deck.getPackName(), deck.getPackIsPublic());
    }
}
