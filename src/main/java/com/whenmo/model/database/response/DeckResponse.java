package com.whenmo.model.database.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import com.whenmo.model.database.domain.Diyer;
import com.whenmo.model.database.domain.DiyerString;
import com.whenmo.model.database.domain.User;
import com.whenmo.model.database.domain.UserDeck;
import com.whenmo.model.database.dto.DeckInfoDto;
import com.whenmo.model.database.dto.UserDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckResponse {
    private UserDto user;
    private DeckInfoDto deckInfo;
    private List<Integer> pack;
    private List<Integer> main;
    private List<Integer> extra;
    private List<Integer> side;

    public static DeckResponse fromModel(UserDeck deckInfo, User user) {
        return DeckResponse.builder().user(UserDto.fromModel(user)).deckInfo(DeckInfoDto.fromModel(deckInfo)).build();

    }
}