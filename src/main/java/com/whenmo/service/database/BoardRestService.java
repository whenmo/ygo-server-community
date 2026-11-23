package com.whenmo.service.database;

import com.whenmo.model.database.domain.Board;
import com.whenmo.model.database.request.BoardEditRequest;
import com.whenmo.model.database.response.BoardResponse;
import com.whenmo.repository.database.BoardRepository;
import com.whenmo.service.RestServiceHelper;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardRestService {

    private final BoardRepository boardRepository;
    private final EntityManager entityManager;

    // ============================================================
    // Read
    // ============================================================

    @Transactional(readOnly = true)
    public List<BoardResponse> getAllBoards() {
        return boardRepository.findAll().stream().map(BoardResponse::fromBoard).toList();
    }

    @Transactional(readOnly = true)
    public BoardResponse getBoardById(Integer boardId) {
        return BoardResponse.fromBoard(getById(boardId));
    }

    // ============================================================
    // Create
    // ============================================================

    @Transactional
    public BoardResponse createBoard(Integer editUserId, BoardEditRequest request) {
        String title = RestServiceHelper.getValidTrimString(request.getTitle(), "公告標題不能為空");

        Board board = request.toBoard(title, editUserId);

        return BoardResponse.fromBoard(refreshBoard(boardRepository.save(board)));
    }

    // ============================================================
    // Update
    // ============================================================

    @Transactional
    public BoardResponse updateBoard(Integer editUserId, Integer boardId, BoardEditRequest request) {
        String title = RestServiceHelper.getValidTrimString(request.getTitle(), "公告標題不能為空");

        Board board = getById(boardId);

        request.updateBoard(board, title, editUserId);

        return BoardResponse.fromBoard(refreshBoard(boardRepository.save(board)));
    }

    // ============================================================
    // Delete
    // ============================================================

    @Transactional
    public void deleteBoard(Integer boardId) {
        boardRepository.delete(getById(boardId));
    }

    // ============================================================
    // Helpers / View
    // ============================================================

    private Board getById(Integer boardId) {
        return boardRepository.findById(boardId).orElseThrow(() -> new RuntimeException("找不到對應的公告ID: " + boardId));
    }

    private Board refreshBoard(Board board) {
        entityManager.flush();
        entityManager.refresh(board);
        return board;
    }
}