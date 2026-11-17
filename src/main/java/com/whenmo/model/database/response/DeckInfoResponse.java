package com.whenmo.model.database.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.whenmo.model.database.domain.*;
import com.whenmo.model.database.dto.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckInfoResponse {
    private UserDto user;
    private DeckInfoDto deckInfo;
    private DeckPackDto deckPack;

    public static DeckInfoResponse fromDeck(Deck deck) {
        if (deck == null)
            return null;
        return new DeckInfoResponse(UserDto.fromDeck(deck), DeckInfoDto.fromDeck(deck), DeckPackDto.fromDeck(deck));
    }
}