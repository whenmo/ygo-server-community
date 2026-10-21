package com.whenmo.service.cdb;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.nio.file.Path;

import org.springframework.stereotype.Service;

import com.whenmo.model.cardinfo.IflistItem;
import com.whenmo.model.database.response.CdbConfigResponse;

@Service
public class CdbIflistService {

    /**
     * 讀取並即時解析 lflist.conf 檔案（每次呼叫皆重新從來源解析）
     */
    private ParsedIflist parseLflist(CdbConfigResponse config) {
        Path parentDir = Path.of(config.getFilePath()).toAbsolutePath().normalize().getParent();
        if (parentDir == null) {
            throw new IllegalStateException("無法取得 CDB 檔案的父目錄");
        }

        Path lflistPath = parentDir.resolve("lflist.conf");
        if (!Files.isRegularFile(lflistPath)) {
            throw new IllegalStateException("在 CDB 同級目錄下找不到 lflist.conf: " + lflistPath);
        }

        Map<String, Map<Integer, List<IflistItem>>> banlistMap = new LinkedHashMap<>();
        Map<String, Map<Integer, IflistItem>> cardStatusIndex = new HashMap<>();

        try {
            List<String> lines = Files.readAllLines(lflistPath, StandardCharsets.UTF_8);

            String currentVersion = null;

            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty()) {
                    continue;
                }

                if (line.startsWith("!")) {
                    currentVersion = line.substring(1).trim();
                    if (!currentVersion.isEmpty()) {
                        banlistMap.putIfAbsent(currentVersion, new LinkedHashMap<>());
                        cardStatusIndex.putIfAbsent(currentVersion, new HashMap<>());
                    } else {
                        currentVersion = null;
                    }
                    continue;
                }

                if (currentVersion == null || line.startsWith("#") || !Character.isDigit(line.charAt(0))) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length < 2) {
                    continue;
                }

                try {
                    int id = Integer.parseInt(parts[0]);
                    int status = Integer.parseInt(parts[1]);
                    if (status < 0 || status > 2) {
                        continue;
                    }

                    IflistItem entry = new IflistItem(id, status);
                    banlistMap.get(currentVersion)
                            .computeIfAbsent(status, k -> new ArrayList<>())
                            .add(entry);
                    cardStatusIndex.get(currentVersion).put(id, entry);
                } catch (NumberFormatException ignored) {
                    // Ignore malformed card IDs or status values.
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ParsedIflist(banlistMap, cardStatusIndex);
    }

    /**
     * 取得所有可用的卡表版本列表
     */
    public Set<String> getAllVersions(CdbConfigResponse configResponse) {
        ParsedIflist parsed = parseLflist(configResponse);
        return parsed.banlistMap.keySet();
    }

    /**
     * 取得特定版本的所有禁限卡分類資料
     */
    public Map<Integer, List<IflistItem>> getBanlistByVersion(CdbConfigResponse configResponse, String versionId) {
        ParsedIflist parsed = parseLflist(configResponse);
        return parsed.banlistMap.getOrDefault(versionId, Collections.emptyMap());
    }

    /**
     * 查詢某張卡片在特定版本中的限制狀態
     */
    public int getCardStatus(CdbConfigResponse configResponse, String versionId, Integer cardId) {
        ParsedIflist parsed = parseLflist(configResponse);
        Map<Integer, IflistItem> versionIndex = parsed.cardStatusIndex.get(versionId);
        if (versionIndex != null && versionIndex.containsKey(cardId)) {
            return versionIndex.get(cardId).getCount();
        }
        return -1; // 無限制
    }

    // 內部輔助記錄類別
    private static class ParsedIflist {
        final Map<String, Map<Integer, List<IflistItem>>> banlistMap;
        final Map<String, Map<Integer, IflistItem>> cardStatusIndex;

        ParsedIflist(Map<String, Map<Integer, List<IflistItem>>> banlistMap,
                Map<String, Map<Integer, IflistItem>> cardStatusIndex) {
            this.banlistMap = banlistMap;
            this.cardStatusIndex = cardStatusIndex;
        }
    }
}