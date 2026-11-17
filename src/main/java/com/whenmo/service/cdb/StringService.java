package com.whenmo.service.cdb;

import com.whenmo.config.ApplicationConfig;
import org.springframework.stereotype.Service;

import com.whenmo.model.cdb.domain.CdbStringsItem;
import com.whenmo.model.cdb.response.StringsResponse;
import com.whenmo.model.database.response.CdbConfigResponse;
import com.whenmo.service.database.CdbConfigRestService;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StringService {

    private final DiskCacheService diskCache;
    private final CdbConfigRestService cdbConfigRestService;
    private final ApplicationConfig applicationConfig;

    // ============================================================
    // Read
    // ============================================================

    /** 根據 cdbId 獲取 strings.conf */
    public StringsResponse getCdbStrings(Integer cdbId) {
        CdbConfigResponse cdbConfig = cdbConfigRestService.getCdbConfig(cdbId);

        StringsResponse cached = diskCache.getCdbStringsCache(cdbConfig);
        if (cached != null)
            return cached;

        String filePath = cdbConfig.getFilePath();
        if (filePath == null || filePath.isBlank())
            throw new IllegalArgumentException("cdb 路徑不可為空");

        Path parentDir = Path.of(filePath).toAbsolutePath().normalize().getParent();
        if (parentDir == null)
            throw new IllegalStateException("無法取得 cdb 檔案的父目錄");

        Path stringsPath = parentDir.resolve("strings.conf");
        if (!Files.isRegularFile(stringsPath))
            throw new IllegalStateException("strings.conf 檔案路徑不合法: " + stringsPath);

        StringsResponse stringsResponse = readStrings(cdbId, stringsPath);
        diskCache.saveCdbStringsCache(cdbConfig, stringsResponse);
        return stringsResponse;
    }

    /** 獲取預設的 strings.conf */
    public synchronized StringsResponse getDefaultStrings() {
        CdbConfigResponse defaultConfig = defaultConfig();

        StringsResponse cached = diskCache.getCdbStringsCache(defaultConfig);
        if (cached != null)
            return cached;

        Path defaultStringsPath = diskCache.getDefaultStringsPath();

        try {
            if (!Files.isRegularFile(defaultStringsPath)) {
                downloadDefaultStrings(defaultStringsPath);
            }
            StringsResponse response = readStrings(-1, defaultStringsPath);
            diskCache.saveCdbStringsCache(defaultConfig, response);
            return response;
        } catch (Exception exception) {
            throw new IllegalStateException("無法讀取預設 strings.conf: " + exception.getMessage(), exception);
        }
    }

    /**
     * 獲取所有 strings 的組合檔案
     * setname、counter 與 victory 中 code 相同時後讀取的會覆蓋先前讀取的
     */
    public StringsResponse getCombineStrings() {
        StringsResponse response = StringsResponse.builder().cdbId(0)
                .setnames(new ArrayList<>())
                .counters(new ArrayList<>())
                .victorys(new ArrayList<>())
                .build();

        List<CdbConfigResponse> cdbConfigs = cdbConfigRestService.getAllConfigs();
        if (cdbConfigs.isEmpty())
            return response;

        StringsResponse cached = diskCache.getCombineCdbStringsCache(defaultConfig(), cdbConfigs);
        if (cached != null)
            return cached;

        Map<Integer, CdbStringsItem> setnames = new LinkedHashMap<>();
        Map<Integer, CdbStringsItem> counters = new LinkedHashMap<>();
        Map<Integer, CdbStringsItem> victorys = new LinkedHashMap<>();

        merge(getDefaultStrings(), setnames, counters, victorys);
        for (CdbConfigResponse config : cdbConfigs) {
            merge(getCdbStrings(config.getId()), setnames, counters, victorys);
        }

        response.setSetnames(new ArrayList<>(setnames.values()));
        response.setCounters(new ArrayList<>(counters.values()));
        response.setVictorys(new ArrayList<>(victorys.values()));

        diskCache.saveCombineCdbStringsCache(defaultConfig(), cdbConfigs, response);
        return response;
    }

    // ============================================================
    // Update / Refresh
    // ============================================================

    /** 刷新 cdbId 的 strings cache */
    public void refreshCdbStrings(Integer cdbId) {
        cdbConfigRestService.getCdbConfig(cdbId);
        diskCache.removeCdbStringsCache(cdbId);
        getCdbStrings(cdbId);
    }

    /** 重新從遠端下載並刷新預設的 default strings.conf cache */
    public synchronized void refreshDefaultStrings() {
        Path defaultStringsPath = diskCache.getDefaultStringsPath();
        try {
            downloadDefaultStrings(defaultStringsPath);
            diskCache.removeCdbStringsCache(-1);
            getDefaultStrings();
        } catch (Exception exception) {
            throw new IllegalStateException("重新下載預設 strings.conf 失敗: " + exception.getMessage(), exception);
        }
    }

    // ============================================================
    // Helpers
    // ============================================================

    /** 讀取 strings.conf */
    private StringsResponse readStrings(Integer cdbId, Path stringsPath) {
        StringsResponse response = StringsResponse.builder()
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
            throw new RuntimeException("讀取 cdbId " + cdbId + " 的 strings.conf 失敗: " + e.getMessage(), e);
        }
    }

    private CdbConfigResponse defaultConfig() {
        return CdbConfigResponse.builder().id(-1).name("default").filePath(diskCache.getDefaultStringsPath().toString())
                .build();
    }

    private void merge(StringsResponse source,
            Map<Integer, CdbStringsItem> setnames,
            Map<Integer, CdbStringsItem> counters,
            Map<Integer, CdbStringsItem> victorys) {
        source.getSetnames().forEach(item -> setnames.put(item.getCode(), item));
        source.getCounters().forEach(item -> counters.put(item.getCode(), item));
        source.getVictorys().forEach(item -> victorys.put(item.getCode(), item));
    }

    private void downloadDefaultStrings(Path targetPath) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(5)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(applicationConfig.getDefaultStringsApi()))
                .timeout(java.time.Duration.ofSeconds(15)).GET().build();
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
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
}