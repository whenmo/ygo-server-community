package com.whenmo.controller.api.database;

import com.whenmo.controller.helper.SecurityHelper;
import com.whenmo.controller.helper.SecurityHelper.RequestAuth;
import com.whenmo.model.database.request.*;
import com.whenmo.model.database.response.*;
import com.whenmo.service.database.BoardRestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardRestController {

    private final BoardRestService boardRestService;
    private final SecurityHelper securityHelper;

    // ============================================================
    // GET
    // ============================================================

    /**
     * 查詢所有公告
     * GET /api/board/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<BoardResponse>> getAllBoards() {
        return ResponseEntity.ok(boardRestService.getAllBoards());
    }

    /**
     * 根據 boardId 查詢公告
     * GET /api/board/id/{boardId}
     */
    @GetMapping("/id/{boardId}")
    public ResponseEntity<BoardResponse> getBoardById(@PathVariable Integer boardId) {
        return ResponseEntity.ok(boardRestService.getBoardById(boardId));
    }

    // ============================================================
    // POST
    // ============================================================

    /**
     * 新增公告
     * POST /api/board
     */
    @PostMapping
    public ResponseEntity<BoardResponse> createConfig(HttpServletRequest httpRequest,
            @Valid @RequestBody BoardEditRequest request) {

        RequestAuth requestAuth = securityHelper.auth(httpRequest);
        HttpStatus httpStatus = requestAuth.resolveHttpStatus(true);
        if (httpStatus != null)
            return ResponseEntity.status(httpStatus).build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(boardRestService.createBoard(requestAuth.getUserId(), request));
    }

    // ============================================================
    // Update
    // ============================================================

    /**
     * 更新公告
     * PUT /api/board/id/{boardId}
     */
    @PutMapping("/id/{boardId}")
    public ResponseEntity<BoardResponse> updateConfig(HttpServletRequest httpRequest,
            @PathVariable Integer boardId,
            @Valid @RequestBody BoardEditRequest request) {

        RequestAuth requestAuth = securityHelper.auth(httpRequest);
        HttpStatus httpStatus = requestAuth.resolveHttpStatus(true);
        if (httpStatus != null)
            return ResponseEntity.status(httpStatus).build();

        return ResponseEntity.ok(boardRestService.updateBoard(requestAuth.getUserId(), boardId, request));
    }

    // ============================================================
    // Delete
    // ============================================================

    /**
     * 刪除公告
     * DELETE /api/board/id/{boardId}
     */
    @DeleteMapping("/id/{boardId}")
    public ResponseEntity<Void> deleteConfig(HttpServletRequest httpRequest, @PathVariable Integer boardId) {

        HttpStatus httpStatus = securityHelper.auth(httpRequest).resolveHttpStatus(true);
        if (httpStatus != null)
            return ResponseEntity.status(httpStatus).build();

        boardRestService.deleteBoard(boardId);
        return ResponseEntity.noContent().build();
    }
}