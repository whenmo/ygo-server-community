package com.whenmo.controller.api.database;

import com.whenmo.model.database.request.*;
import com.whenmo.model.database.response.*;
import com.whenmo.controller.helper.SecurityHelper;
import com.whenmo.service.database.DeckPackRestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deck-packs")
@RequiredArgsConstructor
public class DeckPackRestController {

    private final DeckPackRestService deckPackService;
    private final SecurityHelper securityHelper;

    // ============================================================
    // GET
    // ============================================================

    /**
     * 查詢所有公開卡包
     * GET /api/deck-packs/public
     */
    @GetMapping("/public")
    public ResponseEntity<List<DeckPackResponse>> getPublicDeckPacks() {
        return ResponseEntity.ok(deckPackService.getPublicDeckPacks());
    }

    /**
     * 查詢指定用戶的卡包
     * GET /api/deck-packs/private/{userId}
     */
    @GetMapping("/private/{userId}")
    public ResponseEntity<List<DeckPackResponse>> getUserDeckPacks(@PathVariable Integer userId) {
        return ResponseEntity.ok(deckPackService.getDeckPacksByUserId(userId));
    }

    // ============================================================
    // POST
    // ============================================================

    /**
     * 建立公開卡包
     * POST /api/deck-packs/public
     */
    @PostMapping("/public")
    public ResponseEntity<DeckPackResponse> createPublicDeckPack(HttpServletRequest httpRequest,
            @Valid @RequestBody DeckPackEditRequest request) {

        if (securityHelper.getAuthUserId(httpRequest) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else if (!securityHelper.isAuthAdmin(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        DeckPackResponse deckPack = deckPackService.createPublicDeckPack(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(deckPack);
    }

    /**
     * 建立私人卡包
     * POST /api/deck-packs/private
     */
    @PostMapping("/private")
    public ResponseEntity<DeckPackResponse> createPrivateDeckPack(HttpServletRequest httpRequest,
            @Valid @RequestBody DeckPackEditRequest request) {

        Integer userId = securityHelper.getAuthUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DeckPackResponse deckPack = deckPackService.createPrivateDeckPack(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(deckPack);
    }

    // ============================================================
    // Update
    // ============================================================

    /**
     * 更新公開卡包 (僅限管理員)
     * PUT /api/deck-packs/public/{packId}
     */
    @PutMapping("/public/{packId}")
    public ResponseEntity<DeckPackResponse> updatePublicDeckPack(HttpServletRequest httpRequest,
            @PathVariable Integer packId,
            @Valid @RequestBody DeckPackEditRequest request) {

        if (securityHelper.getAuthUserId(httpRequest) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else if (!securityHelper.isAuthAdmin(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        DeckPackResponse deckPack = deckPackService.updatePublicDeckPack(packId, request);
        return ResponseEntity.ok(deckPack);
    }

    /**
     * 更新私人卡包 (僅限本人)
     * PUT /api/deck-packs/private/{packId}
     */
    @PutMapping("/private/{packId}")
    public ResponseEntity<DeckPackResponse> updatePrivateDeckPack(HttpServletRequest httpRequest,
            @PathVariable Integer packId,
            @Valid @RequestBody DeckPackEditRequest request) {

        Integer userId = securityHelper.getAuthUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DeckPackResponse deckPack = deckPackService.updatePrivateDeckPack(packId, userId, request);
        return ResponseEntity.ok(deckPack);
    }

    // ============================================================
    // Delete
    // ============================================================

    /**
     * 刪除公開卡包 (僅限管理員)
     * DELETE /api/deck-packs/public/{packId}
     */
    @DeleteMapping("/public/{packId}")
    public ResponseEntity<Void> deletePublicDeckPack(HttpServletRequest httpRequest, @PathVariable Integer packId) {
        if (securityHelper.getAuthUserId(httpRequest) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else if (!securityHelper.isAuthAdmin(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        deckPackService.deletePublicDeckPack(packId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 刪除私人卡包 (僅限擁有者)
     * DELETE /api/deck-packs/private/{packId}
     */
    @DeleteMapping("/private/{packId}")
    public ResponseEntity<Void> deleteDeckPack(HttpServletRequest httpRequest, @PathVariable Integer packId) {
        Integer userId = securityHelper.getAuthUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        deckPackService.deletePrivateDeckPack(packId, userId);
        return ResponseEntity.noContent().build();
    }
}