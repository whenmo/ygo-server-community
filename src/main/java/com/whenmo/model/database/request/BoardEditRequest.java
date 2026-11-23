package com.whenmo.model.database.request;

import com.whenmo.model.database.domain.Board;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardEditRequest {

    @NotBlank(message = "公告標題不能為空")
    private String title;

    @Builder.Default
    private Boolean isTop = false;

    private String content;

    public Board toBoard(String trimTitle, Integer editUserId) {
        return Board.builder().isTop(Boolean.TRUE.equals(isTop)).title(trimTitle).content(content)
                .editUserId(editUserId).build();
    }

    public void updateBoard(Board board, String trimTitle, Integer editUserId) {
        if (board == null)
            return;
        board.setIsTop(Boolean.TRUE.equals(isTop));
        board.setTitle(trimTitle);
        board.setContent(content);
        board.setEditUserId(editUserId);
    }
}
