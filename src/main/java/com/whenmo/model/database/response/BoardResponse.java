package com.whenmo.model.database.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.whenmo.model.database.domain.*;
import com.whenmo.model.database.dto.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardResponse {
    private UserDto user;
    private Integer id;
    private Boolean isTop;
    private String title;
    private String content;
    private LocalDateTime updatedAt;

    public static BoardResponse fromBoard(Board board) {
        if (board == null)
            return null;
        return new BoardResponse(UserDto.fromBoard(board), board.getId(), board.getIsTop(), board.getTitle(),
                board.getContent(), board.getUpdatedAt());
    }
}