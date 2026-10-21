package com.whenmo.service.cdb;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whenmo.model.cdb.CdbCardResponse;
import com.whenmo.model.database.response.CdbConfigResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CdbDiskCacheService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.cache.directory:../cache}")
    private String cacheDirectory;

    public List<CdbCardResponse> get(CdbConfigResponse config) {
        try {
            Path source = Path.of(config.getFilePath()).toAbsolutePath().normalize();
            Path cacheFile = cacheFile(config.getId());
            if (!Files.isRegularFile(source) || !Files.isRegularFile(cacheFile)) return null;

            long sourceModified = Files.getLastModifiedTime(source).toMillis();
            JsonNode entry = objectMapper.readTree(cacheFile.toFile());
            if (!source.toString().equals(entry.path("sourcePath").asText())
                    || sourceModified != entry.path("sourceModified").asLong(-1)) {
                return null;
            }
            return objectMapper.convertValue(entry.path("cards"), new TypeReference<List<CdbCardResponse>>() {});
        } catch (Exception exception) {
            return null;
        }
    }

    public void put(CdbConfigResponse config, List<CdbCardResponse> cards) {
        Path temporaryFile = null;
        try {
            Path source = Path.of(config.getFilePath()).toAbsolutePath().normalize();
            Path cacheFile = cacheFile(config.getId());
            Files.createDirectories(cacheFile.getParent());
            temporaryFile = Files.createTempFile(cacheFile.getParent(), "cdb-" + config.getId() + "-", ".tmp");

            Map<String, Object> entry = Map.of(
                    "sourcePath", source.toString(),
                    "sourceModified", Files.getLastModifiedTime(source).toMillis(),
                    "cards", cards);
            objectMapper.writeValue(temporaryFile.toFile(), entry);
            try {
                Files.move(temporaryFile, cacheFile, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, cacheFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("無法寫入 CDB 快取", exception);
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    public void remove(Integer configId) {
        try {
            Files.deleteIfExists(cacheFile(configId));
        } catch (IOException exception) {
            throw new IllegalStateException("無法刪除 CDB 快取", exception);
        }
    }

    private Path cacheFile(Integer configId) {
        return Path.of(cacheDirectory).toAbsolutePath().normalize()
                .resolve("cdb").resolve(configId + ".json");
    }
}