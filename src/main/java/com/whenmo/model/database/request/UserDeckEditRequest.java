package com.whenmo.model.database.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDeckEditRequest {

    private String name;
    private String packName;
    private Boolean isPublic;
    private Integer cdbId;
    private String iflistVersion;
    private List<Integer> mainDeck;
    private List<Integer> extraDeck;
    private List<Integer> sideDeck;
}