package com.whenmo.api.database;

import com.whenmo.model.database.request.*;
import com.whenmo.model.database.response.*;
import com.whenmo.service.database.CdbConfigRestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cdb_config")
@RequiredArgsConstructor
public class CdbConfigRestController {

    private final CdbConfigRestService cdbConfigService;

    // ============================================================
    // Read
    // ============================================================

    /**
     * 查詢所有 CDB 設定
     * GET /api/cdb_config
     */
    @GetMapping
    public ResponseEntity<List<CdbConfigResponse>> getAllConfigs() {
        return ResponseEntity.ok(cdbConfigService.getAllConfigs());
    }

    /**
     * 根據 name 進行模糊查詢
     * GET /api/cdb_config/name/{name}
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<List<CdbConfigResponse>> searchConfigsByName(@PathVariable String name) {
        return ResponseEntity.ok(cdbConfigService.searchConfigsByName(name));
    }

    // ============================================================
    // Create
    // ============================================================

    /**
     * 新增 CDB 設定
     * POST /api/cdb_config
     */
    @PostMapping
    public ResponseEntity<CdbConfigResponse> createConfig(@Valid @RequestBody CdbConfigCreateRequest request) {
        CdbConfigResponse createdConfig = cdbConfigService.createConfig(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdConfig);
    }

    // ============================================================
    // Put
    // ============================================================

    /**
     * 更新 CDB 設定
     * PUT /api/cdb_config/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CdbConfigResponse> updateConfig(
            @PathVariable Integer id,
            @RequestBody CdbConfigUpdateRequest request) {
        return ResponseEntity.ok(cdbConfigService.updateConfig(id, request));
    }

    // ============================================================
    // Delete
    // ============================================================

    /**
     * 刪除 CDB 設定
     * DELETE /api/cdb_config/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable Integer id) {
        cdbConfigService.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 批量刪除 CDB 設定
     * DELETE /api/cdb_config/batch
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteBatchConfigs(@RequestBody List<Integer> ids) {
        cdbConfigService.deleteConfigs(ids);
        return ResponseEntity.ok().build();
    }
}