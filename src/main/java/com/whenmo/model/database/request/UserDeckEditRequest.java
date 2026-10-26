package com.whenmo.model.database.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.whenmo.model.database.dto.DeckInfoDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDeckEditRequest {

    private DeckInfoDto deckInfo;
    private List<Integer> pack;
    private List<Integer> main;
    private List<Integer> extra;
    private List<Integer> side;
}