package com.whenmo.api.cardinfo;

import com.whenmo.model.cardinfo.CardInfoItem;
import com.whenmo.service.cardinfo.CardInfoService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/card_info")
@RequiredArgsConstructor
public class CardInfoRestController {

    private final CardInfoService cardInfoService;

    /**
     * 取得 strings.conf 字段清單
     * GET /api/card_info/default-setnames
     */
    @GetMapping("/default-setnames")
    public ResponseEntity<List<CardInfoItem>> getDefaultSetnames() {
        return ResponseEntity.ok(cardInfoService.getDefaultSetnames());
    }

    /**
     * 取得 strings.conf 指示物清單
     * GET /api/card_info/default-counters
     */
    @GetMapping("/default-counters")
    public ResponseEntity<List<CardInfoItem>> getDefaultCounters() {
        return ResponseEntity.ok(cardInfoService.getDefaultCounters());
    }

    /**
     * 取得 strings.conf 特殊勝利清單
     * GET /api/card_info/default-victorys
     */
    @GetMapping("/default-victorys")
    public ResponseEntity<List<CardInfoItem>> getDefaultVictorys() {
        return ResponseEntity.ok(cardInfoService.getDefaultVictorys());
    }

    /**
     * 刷新 strings.conf 的快取資料
     * POST /api/card_info/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshCache() {
        cardInfoService.refreshCache();
        return ResponseEntity.ok("快取已成功重新整理");
    }
}