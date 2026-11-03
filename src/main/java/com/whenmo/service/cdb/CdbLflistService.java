package com.whenmo.service.cdb;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.nio.file.Path;
import org.springframework.stereotype.Service;

import com.whenmo.model.cdb.response.CdbLflistResponse;
import com.whenmo.model.database.response.CdbConfigResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CdbLflistService {
    private final CdbDiskCacheService diskCache;

    /**
     * 獲取禁卡表
     */
    public List<CdbLflistResponse> getLflists(CdbConfigResponse configResponse) {
        List<CdbLflistResponse> cachedLflists = diskCache.getCdbLflistCache(configResponse);
        if (cachedLflists != null)
            return cachedLflists;

        try {
            List<CdbLflistResponse> iflists = readLflist(configResponse);
            diskCache.putCdbLflistCache(configResponse, iflists);
            return iflists;
        } catch (Exception e) {
            throw new RuntimeException(
                    "讀取 configId " + configResponse.getId() + " 的 lflist.conf 檔案失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 讀取所有 CDB 的禁卡表，並保留每筆資料原本的 cdbId。
     */
    public List<CdbLflistResponse> getAllLflistsCombined(List<CdbConfigResponse> configResponses) {
        if (configResponses == null || configResponses.isEmpty()) {
            return new ArrayList<>();
        }

        List<CdbLflistResponse> combined = diskCache.getAllCdbLflistCache(configResponses);
        if (combined == null) {
            combined = new ArrayList<>();
            for (CdbConfigResponse config : configResponses) {
                combined.addAll(getLflists(config));
            }
            diskCache.putAllCdbLflistCache(configResponses, combined);
        }
        return combined;
    }

    /**
     * 讀取並解析 Lflist.conf 檔案
     */
    private List<CdbLflistResponse> readLflist(CdbConfigResponse configResponse) {
        Objects.requireNonNull(configResponse, "CdbConfigResponse 不可為 null");
        String filePAth = configResponse.getFilePath();
        if (filePAth == null || filePAth.isBlank()) {
            throw new IllegalArgumentException("CDB 檔案路徑不可為空");
        }

        Path parentDir = Path.of(filePAth).toAbsolutePath().normalize().getParent();
        if (parentDir == null) {
            throw new IllegalStateException("無法取得 CDB 檔案的父目錄");
        }

        Path iflistPath = parentDir.resolve("lflist.conf");
        if (!Files.isRegularFile(iflistPath)) {
            throw new IllegalStateException("在 CDB 同級目錄下找不到 lflist.conf: " + iflistPath);
        }

        int cdbId = configResponse.getId().intValue();
        List<CdbLflistResponse> response = new ArrayList<>();

        CdbLflistResponse currentIflist = null;

        try {
            List<String> lines = Files.readAllLines(iflistPath, StandardCharsets.UTF_8);
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
                        currentIflist = CdbLflistResponse.builder()
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

        if (currentIflist != null) {
            response.add(currentIflist);
        }

        return response;
    }
}
