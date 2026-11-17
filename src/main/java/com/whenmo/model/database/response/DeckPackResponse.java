package com.whenmo.model.database.response;

import com.whenmo.model.database.domain.DeckPack;
import com.whenmo.model.database.dto.UserDto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckPackResponse {
    private Integer id;
    private String name;
    private Boolean isPublic;
    private UserDto user;

    public static DeckPackResponse fromDeckPack(DeckPack deckPack) {
        return new DeckPackResponse(
                deckPack.getId(),
                deckPack.getName(),
                deckPack.getIsPublic(),
                UserDto.fromDeckPack(deckPack));
    }
}