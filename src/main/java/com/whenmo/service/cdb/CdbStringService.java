package com.whenmo.service.cdb;

import com.whenmo.config.ApplicationConfig;
import org.springframework.stereotype.Service;

import com.whenmo.model.cdb.domain.CdbStringsItem;
import com.whenmo.model.cdb.response.CdbStringsResponse;
import com.whenmo.model.database.response.CdbConfigResponse;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CdbStringService {

    private final CdbDiskCacheService diskCache;
    private final ApplicationConfig applicationConfig;

    /** 讀取設定檔指定的預設 strings.conf，並使用 cdbid=0 的磁碟快取。 */
    public synchronized CdbStringsResponse getDefaultStrings() {
        CdbConfigResponse defaultConfig = defaultConfig();
        Path defaultStringsPath = diskCache.getDefaultStringsPath();

        CdbStringsResponse cached = diskCache.getCdbStringsCache(defaultConfig);
        if (cached != null) {
            return cached;
        }

        try {
            downloadDefaultStrings(defaultStringsPath);
            CdbStringsResponse response = readStrings(-1, defaultStringsPath);
            diskCache.putCdbStringsCache(defaultConfig, response);
            return response;
        } catch (Exception exception) {
            if (Files.isRegularFile(defaultStringsPath)) {
                CdbStringsResponse response = readStrings(-1, defaultStringsPath);
                diskCache.putCdbStringsCache(defaultConfig, response);
                return response;
            }
            throw new IllegalStateException("無法讀取預設 strings.conf: " + exception.getMessage(), exception);
        }
    }

    public synchronized void refreshDefaultStrings() {
        diskCache.removeCdbStringsCache(-1);
    }

    /**
     * 合併預設 strings 與所有 CDB strings。後讀取的相同 code 會覆蓋先前資料。
     */
    public CdbStringsResponse getAllStringsCombined(List<CdbConfigResponse> configs) {
        Map<Integer, CdbStringsItem> setnames = new LinkedHashMap<>();
        Map<Integer, CdbStringsItem> counters = new LinkedHashMap<>();
        Map<Integer, CdbStringsItem> victorys = new LinkedHashMap<>();

        CdbStringsResponse cached = diskCache.getAllCdbStringsCache(defaultConfig(), configs);
        if (cached != null) {
            return cached;
        }

        merge(getDefaultStrings(), setnames, counters, victorys);
        for (CdbConfigResponse config : configs) {
            merge(getStrings(config), setnames, counters, victorys);
        }

        CdbStringsResponse combined = CdbStringsResponse.builder()
                .cdbId(0)
                .setnames(new ArrayList<>(setnames.values()))
                .counters(new ArrayList<>(counters.values()))
                .victorys(new ArrayList<>(victorys.values()))
                .build();
        diskCache.putAllCdbStringsCache(defaultConfig(), configs, combined);
        return combined;
    }

    private CdbConfigResponse defaultConfig() {
        return CdbConfigResponse.builder()
                .id(-1)
                .name("default")
                .filePath(diskCache.getDefaultStringsPath().toString())
                .build();
    }

    private void merge(CdbStringsResponse source,
            Map<Integer, CdbStringsItem> setnames,
            Map<Integer, CdbStringsItem> counters,
            Map<Integer, CdbStringsItem> victorys) {
        source.getSetnames().forEach(item -> setnames.put(item.getCode(), item));
        source.getCounters().forEach(item -> counters.put(item.getCode(), item));
        source.getVictorys().forEach(item -> victorys.put(item.getCode(), item));
    }

    private void downloadDefaultStrings(Path targetPath) throws IOException, InterruptedException {
        java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5)).build();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(applicationConfig.getDefaultStringsApi()))
                .timeout(java.time.Duration.ofSeconds(15)).GET().build();
        java.net.http.HttpResponse<byte[]> response = client.send(request,
                java.net.http.HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("HTTP " + response.statusCode());
        }

        Files.createDirectories(targetPath.getParent());
        Path temporary = Files.createTempFile(targetPath.getParent(), "strings-", ".tmp");
        try {
            Files.write(temporary, response.body());
            Files.move(temporary, targetPath, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    /**
     * 獲取字段表
     */
    public CdbStringsResponse getStrings(CdbConfigResponse configResponse) {
        Objects.requireNonNull(configResponse, "CdbConfigResponse 不可為 null");

        CdbStringsResponse cachedStrings = diskCache.getCdbStringsCache(configResponse);
        if (cachedStrings != null)
            return cachedStrings;

        String filePAth = configResponse.getFilePath();
        if (filePAth == null || filePAth.isBlank()) {
            throw new IllegalArgumentException("CDB 檔案路徑不可為空");
        }

        Path parentDir = Path.of(filePAth).toAbsolutePath().normalize().getParent();
        if (parentDir == null) {
            throw new IllegalStateException("無法取得 CDB 檔案的父目錄");
        }

        int cdbId = configResponse.getId().intValue();
        try {
            Path stringsPath = parentDir.resolve("strings.conf");
            CdbStringsResponse stringsResponse = readStrings(cdbId, stringsPath);
            diskCache.putCdbStringsCache(configResponse, stringsResponse);
            return stringsResponse;
        } catch (Exception e) {
            throw new RuntimeException(
                    "讀取 configId " + cdbId + " 的 strings.conf 檔案失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 讀取字段表
     */
    private CdbStringsResponse readStrings(int cdbId, Path stringsPath) {
        if (!Files.isRegularFile(stringsPath)) {
            throw new IllegalStateException("strings.conf 檔案路徑不合法: " + stringsPath);
        }

        CdbStringsResponse response = CdbStringsResponse.builder()
                .cdbId(cdbId)
                .setnames(new ArrayList<>())
                .counters(new ArrayList<>())
                .victorys(new ArrayList<>())
                .build();

        try {
            List<String> lines = Files.readAllLines(stringsPath, StandardCharsets.UTF_8);

            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || !line.startsWith("!") || line.startsWith("!sy")) // !system
                    continue;

                String[] parts = line.split("\\s+", 3);
                if (parts.length < 3)
                    continue;

                String type = parts[0];
                String codeHex = parts[1];

                if (!codeHex.matches("(?i)0x[0-9a-f]+"))
                    continue;

                try {
                    int code = Integer.parseInt(codeHex.substring(2), 16);
                    String name = parts[2].split("[\t\\s]+")[0];

                    if (type.equals("!setname")) {
                        response.getSetnames().add(new CdbStringsItem(code, name));
                    } else if (type.equals("!counter")) {
                        response.getCounters().add(new CdbStringsItem(code, name));
                    } else if (type.equals("!victory")) {
                        response.getVictorys().add(new CdbStringsItem(code, name));
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            return response;

        } catch (IOException e) {
            throw new IllegalStateException("strings.conf 檔案讀取失敗: " + stringsPath, e);
        }
    }
}
