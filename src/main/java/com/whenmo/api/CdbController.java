package com.whenmo.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.whenmo.model.cdb.response.CdbCardResponse;
import com.whenmo.model.cdb.response.CdbLflistResponse;
import com.whenmo.model.cdb.response.CdbStringsResponse;
import com.whenmo.model.database.response.CdbConfigResponse;
import com.whenmo.service.cdb.*;
import com.whenmo.service.database.CdbConfigRestService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/cdb")
@RequiredArgsConstructor
public class CdbController {

    private final CdbCardService cdbCardService;
    private final CdbStringService cdbStringService;
    private final CdbLflistService cdbLflistService;
    private final CdbConfigRestService cdbConfigRestService;

    /**
     * 獲取所有 CDB 的組合檔案，當卡片 id 相同時後讀取的會覆蓋先前讀取的
     * GET /api/cdb/card/all
     */
    @GetMapping("/card/all")
    public ResponseEntity<List<CdbCardResponse>> getAllCardsCombined() {
        List<CdbConfigResponse> allConfigs = cdbConfigRestService.getAllConfigs();
        List<CdbCardResponse> combinedCards = cdbCardService.getAllCardsCombined(allConfigs);
        return ResponseEntity.ok(combinedCards);
    }

    /**
     * 根據 configId 獲取 CDB 中的所有卡片
     * GET /api/cdb/card/config/{configId}
     */
    @GetMapping("/card/config/{configId}")
    public ResponseEntity<List<CdbCardResponse>> getCardsByConfigId(@PathVariable Integer configId,
            @RequestParam(defaultValue = "false") boolean refresh) {
        CdbConfigResponse configResponse = cdbConfigRestService.getConfigById(configId);
        return ResponseEntity.ok(cdbCardService.readCdb(configResponse, refresh));
    }

    /**
     * 根據 configId 與 卡片 ID 查詢單張卡片
     * GET /api/cdb/card/config/{configId}/{cardId}
     */
    @GetMapping("/card/config/{configId}/{cardId}")
    public ResponseEntity<CdbCardResponse> getCardById(@PathVariable Integer configId, @PathVariable Integer cardId) {
        CdbConfigResponse configResponse = cdbConfigRestService.getConfigById(configId);
        CdbCardResponse cardResponse = cdbCardService.getCardById(configResponse, cardId);
        return cardResponse == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(cardResponse);
    }

    /**
     * 獲取所有 strings 的組合檔案，當卡片 id 相同時後讀取的會覆蓋先前讀取的
     * GET /api/cdb/strings/config/{configId}
     */
    @GetMapping("/strings/all")
    public ResponseEntity<CdbStringsResponse> getAllStringsCombined() {
        List<CdbConfigResponse> allConfigs = cdbConfigRestService.getAllConfigs();
        return ResponseEntity.ok(cdbStringService.getAllStringsCombined(allConfigs));
    }

    /**
     * 根據 configId 取得同級目錄 strings.conf 中的 setname、counter 與 victory 清單
     * GET /api/cdb/strings/config/{configId}
     */
    @GetMapping("/strings/config/{configId}")
    public ResponseEntity<CdbStringsResponse> getStringsByConfigId(@PathVariable Integer configId) {
        CdbConfigResponse configResponse = cdbConfigRestService.getConfigById(configId);
        return ResponseEntity.ok(cdbStringService.getStrings(configResponse));
    }

    /**
     * 獲取預設的 strings.conf 中的 setname、counter 與 victory 清單
     * GET /api/cdb/strings/default
     */
    @GetMapping("/strings/default")
    public ResponseEntity<CdbStringsResponse> getDefaultStrings() {
        return ResponseEntity.ok(cdbStringService.getDefaultStrings());
    }

    /**
     * 取得 lflist.conf 支援的所有禁限卡版本列表
     * GET /api/cdb/lflist/config/{configId}
     */
    @GetMapping("/lflist/all")
    public ResponseEntity<List<CdbLflistResponse>> getAllLflistsCombined() {
        List<CdbConfigResponse> allConfigs = cdbConfigRestService.getAllConfigs();
        return ResponseEntity.ok(cdbLflistService.getAllLflistsCombined(allConfigs));
    }

    /**
     * 取得 lflist.conf 支援的所有禁限卡版本列表
     * GET /api/cdb/lflist/config/{configId}
     */
    @GetMapping("/lflist/config/{configId}")
    public ResponseEntity<List<CdbLflistResponse>> getLflistsByConfigId(@PathVariable Integer configId) {
        CdbConfigResponse configResponse = cdbConfigRestService.getConfigById(configId);
        return ResponseEntity.ok(cdbLflistService.getLflists(configResponse));
    }
}
