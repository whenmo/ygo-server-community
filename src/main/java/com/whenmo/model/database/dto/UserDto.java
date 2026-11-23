package com.whenmo.model.database.dto;

import com.whenmo.model.database.domain.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Integer id;
    private String name;

    public static UserDto fromUser(User user) {
        if (user == null)
            return null;
        return new UserDto(user.getId(), user.getName());
    }

    public static UserDto fromDeckPack(DeckPack deckPack) {
        if (deckPack == null)
            return null;

        return new UserDto(deckPack.getUserId(), deckPack.getUserName());
    }

    public static UserDto fromDeck(Deck deck) {
        if (deck == null)
            return null;

        return new UserDto(deck.getUserId(), deck.getUserName());
    }

    public static UserDto fromDiyer(Diyer diyer) {
        if (diyer == null)
            return null;

        return new UserDto(diyer.getUserId(), diyer.getUserName());
    }

    public static UserDto fromBoard(Board board) {
        if (board == null)
            return null;

        return new UserDto(board.getEditUserId(), board.getEditUserName());
    }
}