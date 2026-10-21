package com.whenmo.service.cdb;

import org.springframework.stereotype.Service;

import com.whenmo.model.cardinfo.CardInfoItem;
import com.whenmo.model.database.response.CdbConfigResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CdbStringService {

    public Map<String, List<CardInfoItem>> parseStrings(CdbConfigResponse config) {
        Objects.requireNonNull(config, "CdbConfigResponse 不可為 null");
        if (config.getFilePath() == null || config.getFilePath().isBlank()) {
            throw new IllegalArgumentException("CDB 檔案路徑不可為空");
        }

        Path parentDir = Path.of(config.getFilePath()).toAbsolutePath().normalize().getParent();
        if (parentDir == null) {
            throw new IllegalStateException("無法取得 CDB 檔案的父目錄");
        }

        Path stringsPath = parentDir.resolve("strings.conf");
        if (!Files.isRegularFile(stringsPath)) {
            throw new IllegalStateException("在 CDB 同級目錄下找不到 strings.conf: " + stringsPath);
        }

        List<CardInfoItem> setnames = new ArrayList<>();
        List<CardInfoItem> counters = new ArrayList<>();
        List<CardInfoItem> victorys = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(stringsPath, StandardCharsets.UTF_8);

            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || !line.startsWith("!"))
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
                        setnames.add(new CardInfoItem("setname", code, name));
                    } else if (type.equals("!counter")) {
                        counters.add(new CardInfoItem("counter", code, name));
                    } else if (type.equals("!victory")) {
                        victorys.add(new CardInfoItem("victory", code, name));
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            Map<String, List<CardInfoItem>> result = new HashMap<>();
            result.put("setname", setnames.stream().distinct().toList());
            result.put("counter", counters.stream().distinct().toList());
            result.put("victory", victorys.stream().distinct().toList());

            return result;

        } catch (IOException e) {
            throw new IllegalStateException("無法讀取 strings.conf 檔案: " + stringsPath, e);
        }
    }
}