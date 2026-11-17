package com.whenmo.service.cdb;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.nio.file.Path;
import org.springframework.stereotype.Service;

import com.whenmo.model.cdb.response.LflistResponse;
import com.whenmo.model.database.response.CdbConfigResponse;
import com.whenmo.service.database.CdbConfigRestService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LflistService {
    private final DiskCacheService diskCache;
    private final CdbConfigRestService cdbConfigRestService;

    // ============================================================
    // Read
    // ============================================================

    /** 根據 cdbId 獲取 lflist.conf */
    public List<LflistResponse> getCdbLflist(Integer cdbId) {
        CdbConfigResponse cdbConfig = cdbConfigRestService.getCdbConfig(cdbId);

        List<LflistResponse> cached = diskCache.getCdbLflistCache(cdbConfig);
        if (cached != null)
            return cached;

        String filePath = cdbConfig.getFilePath();
        if (filePath == null || filePath.isBlank())
            throw new IllegalArgumentException("cdb 路徑不可為空");

        Path parentDir = Path.of(filePath).toAbsolutePath().normalize().getParent();
        if (parentDir == null)
            throw new IllegalStateException("無法取得 cdb 檔案的父目錄");

        Path lflistPath = parentDir.resolve("lflist.conf");
        if (!Files.isRegularFile(lflistPath))
            throw new IllegalStateException("lflist.conf 檔案路徑不合法: " + lflistPath);

        List<LflistResponse> lflists = readLflist(cdbId, lflistPath);
        diskCache.saveCdbLflistCache(cdbConfig, lflists);
        return lflists;
    }

    /** 獲取所有 lflist.conf 的組合檔案 */
    public List<LflistResponse> getCombineLflist() {
        List<CdbConfigResponse> cdbConfigs = cdbConfigRestService.getAllConfigs();
        if (cdbConfigs.isEmpty())
            return new ArrayList<>();

        List<LflistResponse> cached = diskCache.getCombineLflistCache(cdbConfigs);
        if (cached != null)
            return cached;

        List<LflistResponse> lflists = new ArrayList<>();
        for (CdbConfigResponse config : cdbConfigs) {
            lflists.addAll(getCdbLflist(config.getId()));
        }

        diskCache.saveCombineLflistCache(cdbConfigs, lflists);
        return lflists;
    }
    // ============================================================
    // Update
    // ============================================================

    /** 刷新 cdbId 的 lflist cache */
    public void refreshCdbLflist(Integer cdbId) {
        cdbConfigRestService.getCdbConfig(cdbId);
        diskCache.removeCdbLflistCache(cdbId);
        getCdbLflist(cdbId);
    }

    // ============================================================
    // Helpers
    // ============================================================

    /** 讀取 lflist.conf */
    private List<LflistResponse> readLflist(Integer cdbId, Path lflistPath) {
        List<LflistResponse> response = new ArrayList<>();

        LflistResponse currentIflist = null;

        try {
            List<String> lines = Files.readAllLines(lflistPath, StandardCharsets.UTF_8);
            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("!")) {
                    if (currentIflist != null) {
                        response.add(currentIflist);
                    }

                    String version = line.substring(1).trim();
                    if (!version.isEmpty()) {
                        currentIflist = LflistResponse.builder()
                                .cdbId(cdbId)
                                .version(version)
                                .forbiddenIds(new HashSet<>())
                                .limitIds(new HashSet<>())
                                .semiLimitIds(new HashSet<>())
                                .build();
                    } else {
                        currentIflist = null;
                    }
                    continue;
                }

                if (currentIflist == null) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length < 2)
                    continue;

                try {
                    int cardId = Integer.parseInt(parts[0]);
                    int status = Integer.parseInt(parts[1]);

                    if (cardId <= 0 || (status < 0 || status > 2)) {
                        continue;
                    }

                    if (status == 0) {
                        currentIflist.getForbiddenIds().add(cardId);
                    } else if (status == 1) {
                        currentIflist.getLimitIds().add(cardId);
                    } else if (status == 2) {
                        currentIflist.getSemiLimitIds().add(cardId);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("解析 lflist.conf 失敗: " + e.getMessage(), e);
        }

        if (currentIflist != null)
            response.add(currentIflist);

        return response;
    }
}
