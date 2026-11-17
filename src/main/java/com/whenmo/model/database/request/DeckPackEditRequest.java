package com.whenmo.model.database.request;

import com.whenmo.model.database.domain.DeckPack;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckPackEditRequest {

    @NotBlank(message = "卡包名稱不能為空")
    private String name;

    public DeckPack toDeckPack(String trimName, Integer userId) {
        return DeckPack.builder().name(trimName).userId(userId).build();
    }

    public void updateDeckPack(DeckPack deckPack, String trimName) {
        if (deckPack == null)
            return;
        deckPack.setName(trimName);
    }
}