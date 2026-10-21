package com.whenmo.api;

import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.whenmo.model.cardinfo.CardInfoItem;
import com.whenmo.model.cardinfo.IflistItem;
import com.whenmo.model.cdb.CdbCardResponse;
import com.whenmo.model.database.response.CdbConfigResponse;
import com.whenmo.service.cdb.*;
import com.whenmo.service.database.CdbConfigRestService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/cdb")
@RequiredArgsConstructor
public class CdbController {

    private final CdbCardService cdbCardService;
    private final CdbPicService cdbPicService;
    private final CdbStringService cdbStringService;
    private final CdbIflistService cdbIflistService;
    private final CdbConfigRestService cdbConfigRestService;

    /**
     * 根據 configId 獲取 CDB 中的所有卡片
     * GET /api/cdb/card/{configId}
     */
    @GetMapping("/card/{configId}")
    public ResponseEntity<List<CdbCardResponse>> getCardsByConfigId(
            @PathVariable Integer configId,
            @RequestParam(defaultValue = "false") boolean refresh) {
        CdbConfigResponse configResponse = cdbConfigRestService.getConfigById(configId);
        return ResponseEntity.ok(cdbCardService.readCdb(configResponse, refresh));
    }

    /**
     * 根據 configId 與 卡片 ID 查詢單張卡片
     * GET /api/cdb/card/{configId}/{cardId}
     */
    @GetMapping("/card/{configId}/{cardId}")
    public ResponseEntity<CdbCardResponse> getCardById(@PathVariable Integer configId, @PathVariable Integer cardId) {
        CdbConfigResponse configResponse = cdbConfigRestService.getConfigById(configId);
        CdbCardResponse cardResponse = cdbCardService.getCardById(configResponse, cardId);
        return cardResponse == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(cardResponse);
    }

    /**
     * 根據 configId 與 卡片 ID 查詢單張卡片圖片
     * GET /api/cdb/pic/{configId}/{cardId}
     */
    @GetMapping("/pic/{configId}/{cardId}")
    public ResponseEntity<Resource> getCardPic(@PathVariable Integer configId, @PathVariable Integer cardId) {
        CdbConfigResponse configResponse = cdbConfigRestService.getConfigById(configId);
        List<Resource> resources = cdbPicService.getPicResources(configResponse, cardId);
        if (resources.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resources.get(0));
    }

    /**
     * 根據 configId 取得同級目錄 strings.conf 中的 setname、counter 與 victory 清單
     * GET /api/cdb/string/{configId}
     */
    @GetMapping("/string/{configId}")
    public ResponseEntity<Map<String, List<CardInfoItem>>> getStringByConfigId(@PathVariable Integer configId) {
        CdbConfigResponse configResponse = cdbConfigRestService.getConfigById(configId);
        return ResponseEntity.ok(cdbStringService.parseStrings(configResponse));
    }

    /**
     * 取得 lflist.conf 支援的所有禁限卡版本列表
     * GET /api/cdb/iflist/{configId}/versions
     */
    @GetMapping("/iflist/{configId}/versions")
    public ResponseEntity<Set<String>> getIflistVersions(@PathVariable Integer configId) {
        CdbConfigResponse configResponse = cdbConfigRestService.getConfigById(configId);
        Set<String> versions = cdbIflistService.getAllVersions(configResponse);
        return ResponseEntity.ok(versions);
    }

    /**
     * 取得指定版本（例如 2026.7）的完整禁限卡分類清單
     * GET /api/cdb/iflist/{configId}/version/{versionId}
     */
    @GetMapping("/iflist/{configId}/version/{versionId}")
    public ResponseEntity<Map<Integer, List<IflistItem>>> getIflistByVersion(
            @PathVariable Integer configId,
            @PathVariable String versionId) {
        CdbConfigResponse configResponse = cdbConfigRestService.getConfigById(configId);
        Map<Integer, List<IflistItem>> banlist = cdbIflistService.getBanlistByVersion(configResponse, versionId);
        if (banlist.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(banlist);
    }

    /**
     * 查詢特定卡片在指定禁限卡版本中的張數限制狀態（0:禁止, 1:限制, 2:準限制, -1:無限制）
     * GET /api/cdb/iflist/{configId}/version/{versionId}/card/{cardId}
     */
    @GetMapping("/iflist/{configId}/version/{versionId}/card/{cardId}")
    public ResponseEntity<Integer> checkCardIflistStatus(
            @PathVariable Integer configId,
            @PathVariable String versionId,
            @PathVariable Integer cardId) {
        CdbConfigResponse configResponse = cdbConfigRestService.getConfigById(configId);
        int status = cdbIflistService.getCardStatus(configResponse, versionId, cardId);
        return ResponseEntity.ok(status);
    }
}