package com.whenmo.service.database;

import com.whenmo.model.database.domain.*;
import com.whenmo.model.database.request.*;
import com.whenmo.model.database.response.*;
import com.whenmo.repository.database.CdbConfigRepository;
import com.whenmo.service.cdb.CdbDiskCacheService;
import com.whenmo.service.RestServiceHelper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class CdbConfigRestService {

    private final CdbConfigRepository cdbConfigRepository;
    private final CdbDiskCacheService cdbDiskCache;

    // ============================================================
    // Read
    // ============================================================

    /**
     * 取得所有 CDB 設定
     */
    @Transactional(readOnly = true)
    public List<CdbConfigResponse> getAllConfigs() {
        return toResponse(cdbConfigRepository.findAll());
    }

    /**
     * 根據 ID 取得單筆 CDB 設定
     */
    @Transactional(readOnly = true)
    public CdbConfigResponse getConfigById(int id) {
        return CdbConfigResponse.fromModel(getById(id));
    }

    /**
     * 根據 name 進行模糊查詢
     */
    @Transactional(readOnly = true)
    public List<CdbConfigResponse> searchConfigsByName(String name) {
        RestServiceHelper.validateString(name, "名稱");
        return toResponse(cdbConfigRepository.findByNameContaining(name));
    }

    // ============================================================
    // Create
    // ============================================================

    /**
     * 新增 CDB 設定
     */
    @Transactional
    public CdbConfigResponse createConfig(@Valid @RequestBody CdbConfigCreateRequest request) {
        RestServiceHelper.validateString(request.getName(), "名稱");

        String filePath = request.getFilePath();
        RestServiceHelper.validateFilePath(filePath);

        if (cdbConfigRepository.existsByFilePath(filePath))
            throw new IllegalArgumentException("資料庫已有重複的 CDB 檔案路徑: " + filePath);

        CdbConfig config = request.toModel();
        return CdbConfigResponse.fromModel(cdbConfigRepository.save(config));
    }

    // ============================================================
    // Update
    // ============================================================

    /**
     * 更新 CDB 設定
     */
    @Transactional
    public CdbConfigResponse updateConfig(Integer id, CdbConfigUpdateRequest request) {
        CdbConfig config = getById(id);

        String filePath = request.getFilePath();
        if (filePath != null && !config.getFilePath().equals(filePath)) {
            RestServiceHelper.validateFilePath(filePath);

            if (cdbConfigRepository.existsByFilePath(filePath))
                throw new IllegalArgumentException("資料庫已有重複的 CDB 檔案路徑: " + filePath);
        }

        request.updateModel(config);
        return CdbConfigResponse.fromModel(cdbConfigRepository.save(config));
    }

    // ============================================================
    // Delete
    // ============================================================

    /**
     * 刪除 CDB 設定
     */
    @Transactional
    public void deleteConfig(Integer id) {
        cdbConfigRepository.delete(getById(id));
        cdbDiskCache.remove(id);
    }

    /**
     * 批次刪除 CDB 設定
     */
    @Transactional
    public void deleteConfigs(List<Integer> ids) {
        if (ids == null || ids.isEmpty())
            return;

        List<CdbConfig> configs = cdbConfigRepository.findAllById(ids);
        configs.forEach(config -> cdbDiskCache.remove(config.getId()));
        cdbConfigRepository.deleteAll(configs);
    }

    // ============================================================
    // Helpers
    // ============================================================

    private CdbConfig getById(Integer id) {
        return cdbConfigRepository.findById(id).orElseThrow(() -> new RuntimeException("找不到對應的 CDB 設定，ID: " + id));
    }

    private List<CdbConfigResponse> toResponse(List<CdbConfig> list) {
        return list.stream().map(CdbConfigResponse::fromModel).toList();
    }
}