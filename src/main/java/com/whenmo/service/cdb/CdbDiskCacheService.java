package com.whenmo.service.cdb;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whenmo.model.cdb.response.*;
import com.whenmo.model.database.response.CdbConfigResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CdbDiskCacheService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.cache.directory:../cache}")
    private String cacheDirectory;

    // ============================================================
    // CDB
    // ============================================================

    /**
     * 獲取 cdb 卡片快取
     */
    public List<CdbCardResponse> getCdbCardCache(CdbConfigResponse config) {
        return getCache(config, "cards", new TypeReference<List<CdbCardResponse>>() {
        });
    }

    /**
     * 儲存 cdb 卡片快取
     */
    public void putCdbCardCache(CdbConfigResponse config, List<CdbCardResponse> cards) {
        putCache(config, "cards", cards, "無法寫入 CDB 快取");
    }

    /**
     * 刪除 cdb 卡片快取
     */
    public void removeCdbCardCache(Integer configId) {
        removeCache(configId, "cards", "無法刪除 CDB 快取");
        removeCache(0, "cards", "無法刪除所有 CDB 卡片組合快取");
    }

    public List<CdbCardResponse> getAllCdbCardCache(List<CdbConfigResponse> configs) {
        return getCombinedCache(configs, "cards", new TypeReference<List<CdbCardResponse>>() {
        });
    }

    public void putAllCdbCardCache(List<CdbConfigResponse> configs, List<CdbCardResponse> cards) {
        putCombinedCache(configs, "cards", cards, "無法寫入所有 CDB 卡片快取");
    }

    // ============================================================
    // lflist
    // ============================================================

    /**
     * 獲取 lflist 快取
     */
    public List<CdbLflistResponse> getCdbLflistCache(CdbConfigResponse config) {
        return getCache(config, "lflists", new TypeReference<List<CdbLflistResponse>>() {
        });
    }

    /**
     * 儲存 lflist 快取
     */
    public void putCdbLflistCache(CdbConfigResponse config, List<CdbLflistResponse> lflists) {
        putCache(config, "lflists", lflists, "無法寫入 lflist 快取");
    }

    /**
     * 刪除 lflist 快取
     */
    public void removeCdbLflistCache(Integer configId) {
        removeCache(configId, "lflists", "無法刪除 lflist 快取");
    }

    public List<CdbLflistResponse> getAllCdbLflistCache(List<CdbConfigResponse> configs) {
        return getCombinedCache(configs, "lflists", new TypeReference<List<CdbLflistResponse>>() {
        });
    }

    public void putAllCdbLflistCache(List<CdbConfigResponse> configs, List<CdbLflistResponse> lflists) {
        putCombinedCache(configs, "lflists", lflists, "無法寫入所有 lflist 快取");
    }

    // ============================================================
    // strings
    // ============================================================

    /**
     * 獲取 strings 快取
     */
    public CdbStringsResponse getCdbStringsCache(CdbConfigResponse config) {
        return getCache(config, "strings", new TypeReference<CdbStringsResponse>() {
        });
    }

    /**
     * 儲存 strings 快取
     */
    public void putCdbStringsCache(CdbConfigResponse config, CdbStringsResponse strings) {
        putCache(config, "strings", strings, "無法寫入 strings 快取");
    }

    /**
     * 刪除 strings 快取
     */
    public void removeCdbStringsCache(Integer configId) {
        removeCache(configId, "strings", "無法刪除 strings 快取");
    }

    public CdbStringsResponse getAllCdbStringsCache(
            CdbConfigResponse defaultConfig, List<CdbConfigResponse> configs) {
        List<CdbConfigResponse> allConfigs = new ArrayList<>(configs.size() + 1);
        allConfigs.add(defaultConfig);
        allConfigs.addAll(configs);
        return getCombinedCache(allConfigs, "strings", new TypeReference<CdbStringsResponse>() {
        });
    }

    public void putAllCdbStringsCache(CdbConfigResponse defaultConfig, List<CdbConfigResponse> configs,
            CdbStringsResponse strings) {
        List<CdbConfigResponse> allConfigs = new ArrayList<>(configs.size() + 1);
        allConfigs.add(defaultConfig);
        allConfigs.addAll(configs);
        putCombinedCache(allConfigs, "strings", strings, "無法寫入所有 strings 快取");
    }

    // ============================================================
    // Helpers (共用提取邏輯)
    // ============================================================

    /**
     * 共用快取讀取邏輯
     */
    private <T> T getCache(CdbConfigResponse config, String fileDir, TypeReference<T> typeReference) {
        try {
            if (!isCacheValid(config, fileDir)) {
                return null;
            }
            Path cacheFile = cacheFile(config.getId(), fileDir);
            JsonNode entry = objectMapper.readTree(cacheFile.toFile());
            return objectMapper.convertValue(entry.path(fileDir), typeReference);
        } catch (Exception exception) {
            return null;
        }
    }

    /** 檢查單一來源檔案與其磁碟快取是否仍然對應。 */
    public boolean isCacheValid(CdbConfigResponse config, String fileDir) {
        try {
            if (config == null || config.getId() == null || config.getFilePath() == null
                    || config.getFilePath().isBlank()) {
                return false;
            }
            Path source = Path.of(config.getFilePath()).toAbsolutePath().normalize();
            Path cacheFile = cacheFile(config.getId(), fileDir);
            if (!Files.isRegularFile(source) || !Files.isRegularFile(cacheFile)) {
                return false;
            }
            long sourceModified = Files.getLastModifiedTime(source).toMillis();
            JsonNode entry = objectMapper.readTree(cacheFile.toFile());
            return source.toString().equals(entry.path("sourcePath").asText())
                    && sourceModified == entry.path("sourceModified").asLong(-1)
                    && entry.has(fileDir);
        } catch (Exception exception) {
            return false;
        }
    }

    private <T> T getCombinedCache(List<CdbConfigResponse> configs, String fileDir,
            TypeReference<T> typeReference) {
        try {
            if (configs == null || configs.isEmpty()
                    || configs.stream().anyMatch(config -> !isCacheValid(config, cacheType(fileDir)))) {
                return null;
            }

            Path combinedFile = cacheFile(0, fileDir);
            if (!Files.isRegularFile(combinedFile))
                return null;

            JsonNode entry = objectMapper.readTree(combinedFile.toFile());
            JsonNode sources = entry.path("sources");
            if (entry.path("cacheVersion").asInt(0) != 2 || !matchesSources(configs, sources))
                return null;
            return objectMapper.convertValue(entry.path("data"), typeReference);
        } catch (Exception exception) {
            return null;
        }
    }

    private void putCombinedCache(List<CdbConfigResponse> configs, String fileDir,
            Object dataValue, String errorMessage) {
        Path temporaryFile = null;
        try {
            Path combinedFile = cacheFile(0, fileDir);
            Files.createDirectories(combinedFile.getParent());
            temporaryFile = Files.createTempFile(combinedFile.getParent(), fileDir + "-", ".tmp");
            Map<String, Object> entry = Map.of(
                    "cacheVersion", 2,
                    "sources", sourceEntries(configs),
                    "data", dataValue);
            objectMapper.writeValue(temporaryFile.toFile(), entry);
            try {
                Files.move(temporaryFile, combinedFile, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, combinedFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException(errorMessage, exception);
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private boolean matchesSources(List<CdbConfigResponse> configs, JsonNode sources) {
        if (!sources.isArray() || sources.size() != configs.size())
            return false;
        for (int index = 0; index < configs.size(); index++) {
            CdbConfigResponse config = configs.get(index);
            Path source = Path.of(config.getFilePath()).toAbsolutePath().normalize();
            JsonNode current = sources.get(index);
            try {
                if (!source.toString().equals(current.path("sourcePath").asText())
                        || Files.getLastModifiedTime(source).toMillis() != current.path("sourceModified").asLong(-1)) {
                    return false;
                }
            } catch (IOException exception) {
                return false;
            }
        }
        return true;
    }

    private List<Map<String, Object>> sourceEntries(List<CdbConfigResponse> configs) throws IOException {
        List<Map<String, Object>> sources = new ArrayList<>(configs.size());
        for (CdbConfigResponse config : configs) {
            Path source = Path.of(config.getFilePath()).toAbsolutePath().normalize();
            sources.add(Map.of(
                    "sourcePath", source.toString(),
                    "sourceModified", Files.getLastModifiedTime(source).toMillis()));
        }
        return sources;
    }

    private String cacheType(String combinedFileDir) {
        return switch (combinedFileDir) {
            case "cards", "lflists", "strings" -> combinedFileDir;
            case "all-strings" -> "strings";
            default -> throw new IllegalArgumentException("未知的合併快取類型: " + combinedFileDir);
        };
    }

    /**
     * 共用快取寫入邏輯（含原子寫入與暫存檔保護）
     */
    private void putCache(CdbConfigResponse config, String fileDir, Object dataValue, String errorMessage) {
        Path temporaryFile = null;
        try {
            Path source = Path.of(config.getFilePath()).toAbsolutePath().normalize();
            Path cacheFile = cacheFile(config.getId(), fileDir);
            Files.createDirectories(cacheFile.getParent());
            temporaryFile = Files.createTempFile(cacheFile.getParent(), fileDir + "-" + config.getId() + "-", ".tmp");

            Map<String, Object> entry = Map.of(
                    "sourcePath", source.toString(),
                    "sourceModified", Files.getLastModifiedTime(source).toMillis(),
                    fileDir, dataValue);
            objectMapper.writeValue(temporaryFile.toFile(), entry);
            try {
                Files.move(temporaryFile, cacheFile, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, cacheFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException(errorMessage, exception);
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 共用快取刪除邏輯
     */
    private void removeCache(Integer configId, String fileDir, String errorMessage) {
        try {
            Files.deleteIfExists(cacheFile(configId, fileDir));
        } catch (IOException exception) {
            throw new IllegalStateException(errorMessage, exception);
        }
    }

    /**
     * 從 cacheDirectory 取得快取檔案路徑
     */
    private Path cacheFile(Integer configId, String fileDir) {
        return Path.of(cacheDirectory).toAbsolutePath().normalize()
                .resolve(fileDir).resolve(configId + ".json");
    }
}
