package com.whenmo.controller.api.database;

import com.whenmo.controller.helper.SecurityHelper;
import com.whenmo.model.database.dto.YdkDto;
import com.whenmo.model.database.request.DeckEditRequest;
import com.whenmo.model.database.response.DeckInfoResponse;
import com.whenmo.model.database.response.DeckResponse;
import com.whenmo.service.database.DeckRestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class DeckRestController {

    private final DeckRestService deckService;
    private final SecurityHelper securityHelper;

    // ============================================================
    // GET
    // ============================================================

    /**
     * 查詢所有公開卡組
     * GET /api/decks/public
     */
    @GetMapping("/public")
    public ResponseEntity<List<DeckInfoResponse>> getPublicDecks() {
        return ResponseEntity.ok(deckService.getPublicDecks());
    }

    /**
     * 查詢登入用戶的所有卡組
     * GET /api/decks/mine
     */
    @GetMapping("/mine")
    public ResponseEntity<List<DeckInfoResponse>> getMineDecks(HttpServletRequest httpRequest) {
        Integer userId = securityHelper.getAuthUserId(httpRequest);
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(deckService.getUserDecks(userId));
    }

    /**
     * 查詢指定公開卡組
     * GET /api/decks/public/{deckId}
     */
    @GetMapping("public/{deckId}")
    public ResponseEntity<DeckResponse> getPublicDeck(@PathVariable Integer deckId) {
        return ResponseEntity.ok(deckService.getPublicDeck(deckId));
    }

    /**
     * 查詢登入用戶的指定卡組
     * GET /api/decks/mine/{deckId}
     */
    @GetMapping("/mine/{deckId}")
    public ResponseEntity<DeckResponse> getMineDeck(HttpServletRequest httpRequest, @PathVariable Integer deckId) {
        Integer userId = securityHelper.getAuthUserId(httpRequest);
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(deckService.getUserDeck(userId, deckId));
    }

    /**
     * 下載指定公開卡組
     * GET /api/decks/public/{deckId}/download
     */
    @GetMapping("/public/{deckId}/download")
    public ResponseEntity<Resource> downloadPublicDeck(@PathVariable Integer deckId) {
        YdkDto ydkDto = deckService.getPublicDeckYdkResource(deckId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''"
                                + java.net.URLEncoder.encode(ydkDto.getName(), java.nio.charset.StandardCharsets.UTF_8))
                .body(ydkDto.getResource());
    }

    /**
     * 下載登入用戶的指定卡組
     * GET /api/decks/mine/{deckId}/download
     */
    @GetMapping("/mine/{deckId}/download")
    public ResponseEntity<Resource> downloadMineDeck(HttpServletRequest httpRequest, @PathVariable Integer deckId) {
        Integer userId = securityHelper.getAuthUserId(httpRequest);
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        YdkDto ydkDto = deckService.getUserDeckYdkResource(userId, deckId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''"
                                + java.net.URLEncoder.encode(ydkDto.getName(), java.nio.charset.StandardCharsets.UTF_8))
                .body(ydkDto.getResource());
    }

    // ============================================================
    // POST
    // ============================================================

    /**
     * 用戶建立卡組
     * GET /api/decks
     */
    @PostMapping
    public ResponseEntity<DeckResponse> createDeck(HttpServletRequest httpRequest,
            @Valid @RequestBody DeckEditRequest request) {
        Integer userId = securityHelper.getAuthUserId(httpRequest);
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.status(HttpStatus.CREATED).body(deckService.createDeck(userId, request));
    }

    // ============================================================
    // PUT
    // ============================================================

    /**
     * 用戶更新卡組
     * GET /api/decks/{deckId}
     */
    @PutMapping("/{deckId}")
    public ResponseEntity<DeckResponse> updateDeck(HttpServletRequest httpRequest, @PathVariable Integer deckId,
            @Valid @RequestBody DeckEditRequest request) {
        Integer userId = securityHelper.getAuthUserId(httpRequest);
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(deckService.updateDeck(userId, deckId, request));
    }

    // ============================================================
    // Delete
    // ============================================================

    /**
     * 用戶刪除卡組
     * GET /api/decks/{deckId}
     */
    @DeleteMapping("/{deckId}")
    public ResponseEntity<Void> deleteDeck(HttpServletRequest httpRequest, @PathVariable Integer deckId) {
        Integer userId = securityHelper.getAuthUserId(httpRequest);
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        deckService.deleteDeck(userId, deckId);
        return ResponseEntity.noContent().build();
    }
}