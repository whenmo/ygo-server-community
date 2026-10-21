package com.whenmo.api.database;

import com.whenmo.model.database.request.*;
import com.whenmo.model.database.response.*;
import com.whenmo.service.database.DiyerRestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diyers")
@RequiredArgsConstructor
public class DiyerRestController {

    private final DiyerRestService diyerService;

    // ============================================================
    // Read
    // ============================================================

    /**
     * 查詢所有作者
     * GET /api/diyers
     */
    @GetMapping
    public ResponseEntity<List<DiyerResponse>> getAllDiyers() {
        return ResponseEntity.ok(diyerService.getAllDiyers());
    }

    /**
     * 根據 ID 查詢作者
     * GET /api/diyers/id/{id}
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<DiyerResponse> getDiyerById(@PathVariable Integer id) {
        return ResponseEntity.ok(diyerService.getDiyerById(id));
    }

    /**
     * 根據名稱進行查詢
     * GET /api/diyers/name/{name}?eq=
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<List<DiyerResponse>> getDiyersByName(
            @PathVariable String name,
            @RequestParam(defaultValue = "false") boolean eq) {
        return ResponseEntity.ok(diyerService.searchDiyersByName(name, eq));
    }

    // ============================================================
    // Create
    // ============================================================

    /**
     * 新增作者
     * POST /api/diyers
     */
    @PostMapping
    public ResponseEntity<DiyerResponse> createDiyer(@Valid @RequestBody DiyerCreateRequest request) {
        DiyerResponse createdDiyer = diyerService.createDiyer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDiyer);
    }

    // ============================================================
    // Update
    // ============================================================

    /**
     * 更新作者資訊
     * PUT /api/diyers
     */
    @PutMapping
    public ResponseEntity<DiyerResponse> updateDiyer(@Valid @RequestBody DiyerUpdateRequest request) {
        return ResponseEntity.ok(diyerService.updateDiyer(request));
    }

    // ============================================================
    // Delete
    // ============================================================

    /**
     * 刪除作者
     * DELETE /api/diyers/id/{id}
     */
    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> deleteDiyer(@PathVariable Integer id) {
        diyerService.deleteDiyer(id);
        return ResponseEntity.noContent().build();
    }
}