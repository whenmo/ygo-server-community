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
     * 取得主要類型清單
     * GET /api/card_info/types_main
     */
    @GetMapping("/types_main")
    public ResponseEntity<List<CardInfoItem>> getTypesMain() {
        return ResponseEntity.ok(cardInfoService.getTypesMain());
    }

    /**
     * 取得子類型清單
     * GET /api/card_info/types_monster
     */
    @GetMapping("/types_monster")
    public ResponseEntity<List<CardInfoItem>> getTypesMonster() {
        return ResponseEntity.ok(cardInfoService.getTypesMonster());
    }

    /**
     * 取得子類型清單
     * GET /api/card_info/types_spell_trap
     */
    @GetMapping("/types_spell_trap")
    public ResponseEntity<List<CardInfoItem>> getTypesSpellTrap() {
        return ResponseEntity.ok(cardInfoService.getTypesSpellTrap());
    }

    /**
     * 取得卡片屬性清單 (Attribute)
     * GET /api/card_info/attributes
     */
    @GetMapping("/attributes")
    public ResponseEntity<List<CardInfoItem>> getAttributes() {
        return ResponseEntity.ok(cardInfoService.getAttributes());
    }

    /**
     * 取得卡片種族清單 (Race)
     * GET /api/card_info/races
     */
    @GetMapping("/races")
    public ResponseEntity<List<CardInfoItem>> getRaces() {
        return ResponseEntity.ok(cardInfoService.getRaces());
    }

    /**
     * 取得卡片效果分類清單 (Category)
     * GET /api/card_info/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CardInfoItem>> getCategories() {
        return ResponseEntity.ok(cardInfoService.getCategories());
    }

    /**
     * 取得卡片規則/所屬清單 (Rule / OT)
     * GET /api/card_info/rules
     */
    @GetMapping("/rules")
    public ResponseEntity<List<CardInfoItem>> getRules() {
        return ResponseEntity.ok(cardInfoService.getRules());
    }

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