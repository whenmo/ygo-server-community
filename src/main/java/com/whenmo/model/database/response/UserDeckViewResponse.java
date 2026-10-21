package com.whenmo.model.database.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeckViewResponse {

    private UserDeckResponse deck;
    private List<Integer> mainDeck;
    private List<Integer> extraDeck;
    private List<Integer> sideDeck;
}