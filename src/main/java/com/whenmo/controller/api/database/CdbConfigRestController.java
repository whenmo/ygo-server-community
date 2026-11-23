package com.whenmo.controller.api.database;

import com.whenmo.controller.helper.SecurityHelper;
import com.whenmo.model.database.request.*;
import com.whenmo.model.database.response.*;
import com.whenmo.service.database.CdbConfigRestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cdb-config")
@RequiredArgsConstructor
public class CdbConfigRestController {

    private final CdbConfigRestService cdbConfigService;
    private final SecurityHelper securityHelper;

    // ============================================================
    // GET
    // ============================================================

    /**
     * 查詢所有 CDB 設定
     * GET /api/cdb-config/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<CdbConfigResponse>> getAllConfigs() {
        return ResponseEntity.ok(cdbConfigService.getAllConfigs());
    }

    /**
     * 根據 cdbId 查詢 CDB 設定
     * GET /api/cdb-config/id/{cdbId}
     */
    @GetMapping("/id/{cdbId}")
    public ResponseEntity<CdbConfigResponse> getCdbConfig(@PathVariable Integer cdbId) {
        return ResponseEntity.ok(cdbConfigService.getCdbConfig(cdbId));
    }

    // ============================================================
    // POST
    // ============================================================

    /**
     * 新增 CDB 設定
     * POST /api/cdb-config
     */
    @PostMapping
    public ResponseEntity<CdbConfigResponse> createConfig(HttpServletRequest httpRequest,
            @Valid @RequestBody CdbConfigEditRequest request) {

        HttpStatus httpStatus = securityHelper.auth(httpRequest).resolveHttpStatus(true);
        if (httpStatus != null)
            return ResponseEntity.status(httpStatus).build();

        CdbConfigResponse createdConfig = cdbConfigService.createConfig(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdConfig);
    }

    // ============================================================
    // Update
    // ============================================================

    /**
     * 更新 CDB 設定
     * PUT /api/cdb-config/id/{cdbId}
     */
    @PutMapping("/id/{cdbId}")
    public ResponseEntity<CdbConfigResponse> updateConfig(HttpServletRequest httpRequest,
            @PathVariable Integer cdbId,
            @RequestBody CdbConfigEditRequest request) {

        HttpStatus httpStatus = securityHelper.auth(httpRequest).resolveHttpStatus(true);
        if (httpStatus != null)
            return ResponseEntity.status(httpStatus).build();

        return ResponseEntity.ok(cdbConfigService.updateConfig(cdbId, request));
    }

    // ============================================================
    // Delete
    // ============================================================

    /**
     * 刪除 CDB 設定
     * DELETE /api/cdb-config/id/{id}
     */
    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> deleteConfig(HttpServletRequest httpRequest, @PathVariable Integer id) {

        HttpStatus httpStatus = securityHelper.auth(httpRequest).resolveHttpStatus(true);
        if (httpStatus != null)
            return ResponseEntity.status(httpStatus).build();

        cdbConfigService.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }
}