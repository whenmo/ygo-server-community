package com.whenmo.model.database.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import com.whenmo.model.database.domain.Deck;
import com.whenmo.model.database.dto.DeckInfoDto;
import com.whenmo.model.database.dto.DeckPackDto;
import com.whenmo.model.database.dto.UserDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckResponse {
    private UserDto user;
    private DeckInfoDto deckInfo;
    private DeckPackDto deckPack;
    private List<Integer> pack;
    private List<Integer> main;
    private List<Integer> extra;
    private List<Integer> side;

    public static DeckResponse fromDeckAndMap(Deck deck, List<Integer> pack, List<Integer> main, List<Integer> extra,
            List<Integer> side) {
        if (deck == null)
            return null;
        return new DeckResponse(
                UserDto.fromDeck(deck),
                DeckInfoDto.fromDeck(deck),
                DeckPackDto.fromDeck(deck),
                pack, main, extra, side);
    }
}