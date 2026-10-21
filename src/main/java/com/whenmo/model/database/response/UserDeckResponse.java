package com.whenmo.model.database.response;

import com.whenmo.model.database.domain.UserDeck;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeckResponse {

    private Integer id;
    private Integer userId;
    private String userName;
    private String name;
    private String packName;
    private Boolean isPublic;
    private LocalDateTime updatedAt;

    public static UserDeckResponse fromModel(UserDeck model, String userName) {
        return UserDeckResponse.builder()
                .id(model.getId())
                .userId(model.getUserId())
                .userName(userName)
                .name(model.getName())
                .packName(model.getPackName())
                .isPublic(model.getIsPublic())
                .updatedAt(model.getUpdatedAt())
                .build();
    }
}