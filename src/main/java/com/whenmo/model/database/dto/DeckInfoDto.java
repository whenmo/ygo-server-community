package com.whenmo.model.database.dto;

import java.time.LocalDateTime;

import com.whenmo.model.database.domain.Diyer;
import com.whenmo.model.database.domain.UserDeck;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DeckInfoDto {
    private Integer id;
    private String name;
    private String packName;
    private Boolean isPublic;
    private LocalDateTime updatedAt;

    public static DeckInfoDto fromModel(UserDeck model) {
        if (model == null)
            return null;
        return DeckInfoDto.builder().id(model.getId()).name(model.getName()).packName(model.getPackName())
                .isPublic(model.getIsPublic()).updatedAt(model.getUpdatedAt()).build();
    }
}
