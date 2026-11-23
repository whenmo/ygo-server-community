package com.whenmo.controller.api.database;

import com.whenmo.controller.helper.SecurityHelper;
import com.whenmo.model.database.request.*;
import com.whenmo.model.database.response.*;
import com.whenmo.service.database.DiyerRestService;

import jakarta.servlet.http.HttpServletRequest;
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
    private final SecurityHelper securityHelper;

    // ============================================================
    // GET
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
     * GET /api/diyers/id/{diyerId}
     */
    @GetMapping("/id/{diyerId}")
    public ResponseEntity<DiyerResponse> getDiyerById(@PathVariable Integer diyerId) {
        return ResponseEntity.ok(diyerService.getDiyerById(diyerId));
    }

    /**
     * 根據 cdbId 查詢作者列表
     * GET /api/diyers/cdb/{cdbId}
     */
    @GetMapping("/cdb/{cdbId}")
    public ResponseEntity<List<DiyerResponse>> getDiyerByCdbId(@PathVariable Integer cdbId) {
        return ResponseEntity.ok(diyerService.getDiyersByCdbId(cdbId));
    }

    /**
     * 根據 userId 查詢作者列表
     * GET /api/diyers/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DiyerResponse>> getDiyerByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(diyerService.getDiyersByUserId(userId));
    }

    // ============================================================
    // POST
    // ============================================================

    /**
     * 新增作者
     * POST /api/diyers
     */
    @PostMapping
    public ResponseEntity<DiyerResponse> createDiyer(HttpServletRequest httpRequest,
            @Valid @RequestBody DiyerCreateRequest request) {

        HttpStatus httpStatus = securityHelper.auth(httpRequest).resolveHttpStatus(true);
        if (httpStatus != null)
            return ResponseEntity.status(httpStatus).build();

        DiyerResponse diyer = diyerService.createDiyer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(diyer);
    }

    // ============================================================
    // PUT
    // ============================================================

    /**
     * 更新作者資訊
     * PUT /api/diyers/{diyerId}
     */
    @PutMapping("/id/{diyerId}")
    public ResponseEntity<DiyerResponse> updateDiyer(HttpServletRequest httpRequest,
            @PathVariable Integer diyerId,
            @Valid @RequestBody DiyerUpdateRequest request) {

        HttpStatus httpStatus = securityHelper.auth(httpRequest).resolveHttpStatus(true);
        if (httpStatus != null)
            return ResponseEntity.status(httpStatus).build();

        return ResponseEntity.ok(diyerService.updateDiyer(diyerId, request));
    }

    // ============================================================
    // Delete
    // ============================================================

    /**
     * 刪除作者
     * DELETE /api/diyers/id/{id}
     */
    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> deleteDiyer(HttpServletRequest httpRequest, @PathVariable Integer id) {

        HttpStatus httpStatus = securityHelper.auth(httpRequest).resolveHttpStatus(true);
        if (httpStatus != null)
            return ResponseEntity.status(httpStatus).build();

        diyerService.deleteDiyer(id);
        return ResponseEntity.noContent().build();
    }
}