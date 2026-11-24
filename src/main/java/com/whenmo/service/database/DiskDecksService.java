package com.whenmo.service.database;

import com.whenmo.model.database.request.DeckEditRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiskDecksService {

    @Value("${app.decks.directory:./decks}")
    private String deckDirectory;

    /**
     * 解析 YDK 檔案內容至對應的卡片列表分類中
     */
    public Map<String, List<Integer>> parseYdkContent(Integer deckId) {
        Path filePath = getDeckPath(deckId);
        Map<String, List<Integer>> result = new HashMap<>();
        List<Integer> pack = new ArrayList<>();
        List<Integer> main = new ArrayList<>();
        List<Integer> extra = new ArrayList<>();
        List<Integer> side = new ArrayList<>();

        result.put("pack", pack);
        result.put("main", main);
        result.put("extra", extra);
        result.put("side", side);

        try {
            if (!Files.exists(filePath))
                return result;

            List<String> lines = Files.readAllLines(filePath);
            List<Integer> current = pack;

            for (String line : lines) {
                String value = line.trim();
                if (value.equalsIgnoreCase("#main")) {
                    current = main;
                } else if (value.equalsIgnoreCase("#extra")) {
                    current = extra;
                } else if (value.equalsIgnoreCase("!side")) {
                    current = side;
                } else if (value.matches("\\d+")) {
                    current.add(Integer.parseInt(value));
                }
            }
        } catch (IOException ignored) {
        }
        result.put("pack", formatCardIds(result.get("pack"), 90));
        result.put("main", formatCardIds(result.get("main"), 60));
        result.put("extra", formatCardIds(result.get("extra"), 15));
        result.put("side", formatCardIds(result.get("side"), 15));
        return result;
    }

    /**
     * 載入卡組 YDK 檔案資源
     */
    public Resource getYdkResource(Integer deckId) {
        try {
            Path filePath = getDeckPath(deckId);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new IllegalStateException("伺服器上找不到對應的 YDK 檔案");
        } catch (Exception e) {
            throw new IllegalStateException("無法讀取 YDK 檔案", e);
        }
    }

    /**
     * 儲存卡組 YDK 檔案，以卡組 ID 作為檔名
     */
    public void saveYdkFile(Integer deckId, String userName, DeckEditRequest request) {

        Path filePath = getDeckPath(deckId);
        boolean copied = false;
        String contents = getYdkContent(userName, request.getPack(), request.getMain(), request.getExtra(),
                request.getSide());
        try {
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, contents);
            copied = true;
        } catch (IOException | RuntimeException exception) {
            if (copied) {
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException ignored) {
                    exception.addSuppressed(ignored);
                }
            }
            if (exception instanceof RuntimeException runtimeException)
                throw runtimeException;
            throw new IllegalStateException("無法儲存 YDK 檔案", exception);
        }
    }

    /**
     * 刪除指定卡組的 YDK 檔案
     */
    public void deleteYdkFile(Integer deckId) {
        Path filePath = getDeckPath(deckId);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new IllegalStateException("無法刪除 YDK 檔案", exception);
        }
    }

    // ============================================================
    // Helper
    // ============================================================

    /**
     * 根據卡組 ID 取得對應的檔案儲存絕對路徑
     */
    private Path getDeckPath(Integer deckId) {
        Path root = Path.of(deckDirectory).toAbsolutePath().normalize();
        Path filePath = root.resolve(deckId + ".ydk").normalize();
        if (!filePath.startsWith(root)) {
            throw new IllegalArgumentException("卡組檔案路徑無效");
        }
        return filePath;
    }

    /**
     * 解析 YDK 檔案內容至對應的卡片列表分類中
     */
    private String getYdkContent(String userName, List<Integer> pack, List<Integer> main,
            List<Integer> extra, List<Integer> side) {

        pack = formatCardIds(pack, 90);
        main = formatCardIds(main, 60);
        extra = formatCardIds(extra, 15);
        side = formatCardIds(side, 15);

        StringBuilder content = new StringBuilder("#created by " + userName + "\n");
        if (!pack.isEmpty()) {
            pack.forEach(idVal -> content.append(idVal).append('\n'));
        } else {
            content.append("#main\n");
            main.forEach(idVal -> content.append(idVal).append('\n'));
            content.append("#extra\n");
            extra.forEach(idVal -> content.append(idVal).append('\n'));
            content.append("!side\n");
            side.forEach(idVal -> content.append(idVal).append('\n'));
        }

        return content.toString();
    }

    /**
     * 格式化卡片 ID 列表，並限制每種卡片的最大數量
     */
    public List<Integer> formatCardIds(List<Integer> cardIds, int maxCount) {
        if (cardIds == null) {
            return new ArrayList<>();
        } else if (cardIds.size() <= maxCount) {
            return cardIds;
        }

        List<Integer> formatCardIds = new ArrayList<>();
        for (Integer cardId : cardIds) {
            if (formatCardIds.size() >= maxCount) {
                break;
            } else if (cardId == null) {
                continue;
            }
            formatCardIds.add(cardId);
        }

        return formatCardIds;
    }
}
