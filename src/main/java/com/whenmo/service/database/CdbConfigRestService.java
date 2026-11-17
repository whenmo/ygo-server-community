package com.whenmo.service.database;

import com.whenmo.model.database.domain.*;
import com.whenmo.model.database.request.*;
import com.whenmo.model.database.response.*;
import com.whenmo.repository.database.CdbConfigRepository;
import com.whenmo.service.cdb.DiskCacheService;
import com.whenmo.service.RestServiceHelper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CdbConfigRestService {

    private final CdbConfigRepository cdbConfigRepository;
    private final DiskCacheService cdbDiskCache;

    // ============================================================
    // Read
    // ============================================================

    /**
     * 取得所有 CDB 設定
     */
    @Transactional(readOnly = true)
    public List<CdbConfigResponse> getAllConfigs() {
        return cdbConfigRepository.findAll().stream().map(CdbConfigResponse::fromCdbConfig).toList();
    }

    /**
     * 取得單個 CDB 設定
     */
    @Transactional(readOnly = true)
    public CdbConfigResponse getCdbConfig(Integer configId) {
        return CdbConfigResponse.fromCdbConfig(getById(configId));
    }

    // ============================================================
    // Create
    // ============================================================

    /**
     * 新增 CDB 設定
     */
    @Transactional
    public CdbConfigResponse createConfig(CdbConfigEditRequest request) {

        String baseName = RestServiceHelper.getValidTrimString(request.getName(), "顯示名稱不可為空");
        String name = baseName;
        int i = 1;

        while (cdbConfigRepository.existsByName(name)) {
            name = baseName + "_" + i;
            i++;
        }

        String filePath = request.getFilePath();
        RestServiceHelper.validateFilePath(filePath);

        if (cdbConfigRepository.existsByFilePath(filePath))
            throw new IllegalArgumentException("資料庫已有重複的 CDB 檔案路徑: " + filePath);

        CdbConfig config = request.toCdbConfig(name);
        return CdbConfigResponse.fromCdbConfig(cdbConfigRepository.save(config));
    }

    // ============================================================
    // Update
    // ============================================================

    /**
     * 更新 CDB 設定
     */
    @Transactional
    public CdbConfigResponse updateConfig(Integer cdbId, CdbConfigEditRequest request) {
        CdbConfig config = getById(cdbId);

        String baseName = RestServiceHelper.getValidTrimString(request.getName(), "顯示名稱不可為空");
        String name = baseName;
        if (!config.getName().equals(name)) {
            int i = 1;

            while (cdbConfigRepository.existsByName(name)) {
                name = baseName + "_" + i;
                i++;
            }
        }

        String filePath = request.getFilePath();
        if (!config.getFilePath().equals(filePath)) {
            RestServiceHelper.validateFilePath(filePath);

            if (cdbConfigRepository.existsByFilePath(filePath))
                throw new IllegalArgumentException("資料庫已有重複的 CDB 檔案路徑: " + filePath);
        }

        request.updateCdbConfig(config, name);
        cdbDiskCache.removeCdbCardCache(cdbId);
        cdbDiskCache.removeCdbStringsCache(cdbId);
        cdbDiskCache.removeCdbLflistCache(cdbId);
        return CdbConfigResponse.fromCdbConfig(cdbConfigRepository.save(config));
    }

    // ============================================================
    // Delete
    // ============================================================

    /**
     * 刪除 CDB 設定
     */
    @Transactional
    public void deleteConfig(Integer cdbId) {
        cdbConfigRepository.delete(getById(cdbId));
        cdbDiskCache.removeCdbCardCache(cdbId);
        cdbDiskCache.removeCdbStringsCache(cdbId);
        cdbDiskCache.removeCdbLflistCache(cdbId);
    }

    // ============================================================
    // Helpers
    // ============================================================

    private CdbConfig getById(Integer cdbId) {
        return cdbConfigRepository.findById(cdbId)
                .orElseThrow(() -> new RuntimeException("找不到對應的 CDB 設定，ID: " + cdbId));
    }
}