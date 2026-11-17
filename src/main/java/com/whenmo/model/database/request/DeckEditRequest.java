package com.whenmo.model.database.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

import com.whenmo.model.database.domain.Deck;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeckEditRequest {

    @NotBlank(message = "卡組名稱不能為空")
    private String name;
    @Builder.Default
    private Boolean isPublic = false;
    @Builder.Default
    private Boolean isFavor = false;
    @Builder.Default
    private Integer packId = null;
    @Builder.Default
    private List<Integer> pack = new ArrayList<>();
    @Builder.Default
    private List<Integer> main = new ArrayList<>();
    @Builder.Default
    private List<Integer> extra = new ArrayList<>();
    @Builder.Default
    private List<Integer> side = new ArrayList<>();

    public Deck toDeck(String trimName, Integer userId, Integer packId) {
        return Deck.builder().name(trimName).isPublic(isPublic).isFavor(isFavor).userId(userId).packId(packId).build();
    }

    public void updateDeck(Deck deck, String trimName, Integer packId) {
        if (deck == null)
            return;
        deck.setName(trimName);
        deck.setIsPublic(isPublic);
        deck.setIsFavor(isFavor);
        deck.setPackId(packId);
    }
}