package com.whenmo.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.whenmo.model.cdb.response.CardResponse;
import com.whenmo.model.cdb.response.LflistResponse;
import com.whenmo.model.cdb.response.StringsResponse;
import com.whenmo.service.cdb.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/cdb")
@RequiredArgsConstructor
public class CdbController {

    private final CardService cdbCardService;
    private final StringService cdbStringService;
    private final LflistService cdbLflistService;

    // ============================================================
    // Card
    // ============================================================

    /**
     * 根據 cdbId 獲取 CDB 中的所有卡片
     * GET /api/cdb/id/{cdbId}/card
     */
    @GetMapping("/id/{cdbId}/card")
    public ResponseEntity<List<CardResponse>> getCdbCard(@PathVariable Integer cdbId) {
        return ResponseEntity.ok(cdbCardService.getCdbCard(cdbId));
    }

    /**
     * 根據 cdbId 與 cardId 查詢單張卡片
     * GET /api/cdb/id/{cdbId}/card/{cardId}
     */
    @GetMapping("/id/{cdbId}/card/{cardId}")
    public ResponseEntity<CardResponse> getCdbCardById(@PathVariable Integer cdbId, @PathVariable Integer cardId) {
        return ResponseEntity.ok(cdbCardService.getCdbCardById(cdbId, cardId));
    }

    /**
     * 根據 cardId 查詢單張卡片
     * GET /api/cdb/card/{cardId}
     */
    @GetMapping("/card/{cardId}")
    public ResponseEntity<CardResponse> getCardById(@PathVariable Integer cardId) {
        return ResponseEntity.ok(cdbCardService.getCardById(cardId));
    }

    /**
     * 獲取所有 CDB 的組合檔案，當卡片 id 相同時後讀取的會覆蓋先前讀取的
     * GET /api/cdb/card/all
     */
    @GetMapping("/card/all")
    public ResponseEntity<List<CardResponse>> getCombineCdbCard() {
        return ResponseEntity.ok(cdbCardService.getCombineCdbCard());
    }

    /**
     * 刷新 cdbId 的 卡片cache
     * POST /api/cdb/id/{cdbId}/card/refresh
     */
    @PostMapping("/id/{cdbId}/card/refresh")
    public ResponseEntity<Boolean> refreshCdbCard(@PathVariable Integer cdbId) {
        cdbCardService.refreshCdbCard(cdbId);
        return ResponseEntity.ok(Boolean.TRUE);
    }

    // ============================================================
    // Strings
    // ============================================================

    /**
     * 根據 cdbId 獲取 strings.conf
     * GET /api/cdb/id/{cdbId}/strings
     */
    @GetMapping("/id/{cdbId}/strings")
    public ResponseEntity<StringsResponse> getCdbStrings(@PathVariable Integer cdbId) {
        return ResponseEntity.ok(cdbStringService.getCdbStrings(cdbId));
    }

    /**
     * 獲取預設的 strings.conf
     * GET /api/cdb/strings/default
     */
    @GetMapping("/strings/default")
    public ResponseEntity<StringsResponse> getDefaultStrings() {
        return ResponseEntity.ok(cdbStringService.getDefaultStrings());
    }

    /**
     * 獲取所有 strings.conf 的組合檔案
     * setname、counter 與 victory 中 code 相同時後讀取的會覆蓋先前讀取的
     * GET /api/cdb/strings/all
     */
    @GetMapping("/strings/all")
    public ResponseEntity<StringsResponse> getCombineStrings() {
        return ResponseEntity.ok(cdbStringService.getCombineStrings());
    }

    /**
     * 刷新 cdbId 的 strings cache
     * POST /api/cdb/id/{cdbId}/strings/refresh
     */
    @PostMapping("/id/{cdbId}/strings/refresh")
    public ResponseEntity<Boolean> refreshCdbStrings(@PathVariable Integer cdbId) {
        cdbStringService.refreshCdbStrings(cdbId);
        return ResponseEntity.ok(Boolean.TRUE);
    }

    /**
     * 重新從遠端下載 default strings.conf
     * POST /api/cdb/id/{cdbId}/cards/refresh
     */
    @PostMapping("/strings/default/refresh")
    public ResponseEntity<Boolean> refreshDefaultStrings() {
        cdbStringService.refreshDefaultStrings();
        return ResponseEntity.ok(Boolean.TRUE);
    }

    // ============================================================
    // Lflist
    // ============================================================

    /**
     * 根據 cdbId 獲取 lflist.conf
     * GET /api/cdb/id/{cdbId}/lflist
     */
    @GetMapping("/id/{cdbId}/lflist")
    public ResponseEntity<List<LflistResponse>> getCdbLflist(@PathVariable Integer cdbId) {
        return ResponseEntity.ok(cdbLflistService.getCdbLflist(cdbId));
    }

    /**
     * 獲取所有 lflist.conf 的組合檔案
     * GET /api/cdb/lflist/all
     */
    @GetMapping("/lflist/all")
    public ResponseEntity<List<LflistResponse>> getCombineLflist() {
        return ResponseEntity.ok(cdbLflistService.getCombineLflist());
    }

    /**
     * 刷新 cdbId 的 lflist cache
     * POST /api/cdb/id/{cdbId}/lflist/refresh
     */
    @PostMapping("/id/{cdbId}/lflist/refresh")
    public ResponseEntity<Boolean> refreshCdbLflist(@PathVariable Integer cdbId) {
        cdbLflistService.refreshCdbLflist(cdbId);
        return ResponseEntity.ok(Boolean.TRUE);
    }
}
