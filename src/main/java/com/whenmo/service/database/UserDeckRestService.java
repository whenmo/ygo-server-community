package com.whenmo.service.database;

import com.whenmo.model.database.domain.User;
import com.whenmo.model.database.domain.DeckPack;
import com.whenmo.model.database.domain.UserDeck;
import com.whenmo.model.cardinfo.IflistItem;
import com.whenmo.model.cdb.CdbCardResponse;
import com.whenmo.model.database.response.CdbConfigResponse;
import com.whenmo.model.database.request.DeckPackCreateRequest;
import com.whenmo.model.database.request.UserDeckEditRequest;
import com.whenmo.model.database.response.DeckResponse;
import com.whenmo.model.database.response.UserResponse;
import com.whenmo.repository.database.UserDeckRepository;
import com.whenmo.repository.database.DeckPackRepository;
import com.whenmo.repository.database.UserRepository;
import com.whenmo.service.cdb.CdbCardService;
import com.whenmo.service.cdb.CdbIflistService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDeckRestService {

    private static final Pattern VALID_NAME = Pattern.compile("[\\p{L}\\p{N} _().-]{1,80}");

    private final UserDeckRepository deckRepository;
    private final UserRepository userRepository;
    private final DeckPackRepository deckPackRepository;

    @Value("${app.user-decks.storage-dir:../decks}")
    private String deckStorageDirectory;

    // ============================================================
    // Read
    // ============================================================

    @Transactional(readOnly = true)
    public List<DeckResponse> getUserDecks(Integer userId) {
        User user = getUser(userId);
        return deckRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(deck -> buildDeckResponse(deck, user)).toList();
    }

    @Transactional(readOnly = true)
    public List<DeckResponse> getPublicDecks() {
        Map<Integer, User> usersMap = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return deckRepository.findByIsPublicTrueOrderByUpdatedAtDesc().stream()
                .map(deck -> buildDeckResponse(deck, usersMap.get(deck.getUserId()))).toList();
    }

    @Transactional(readOnly = true)
    public List<String> getDeckPacks() {
        return deckPackRepository.findAll().stream().map(DeckPack::getName).sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getDeckUsers() {
        List<Integer> userIds = deckRepository.findAllDeckUserIds();
        if (userIds.isEmpty())
            return List.of();
        return userRepository.findAllById(userIds).stream().map(UserResponse::fromModel).toList();
    }

    @Transactional(readOnly = true)
    public Optional<DeckResponse> getDeckForView(Integer ownerId, Integer deckId, Integer viewerId) {
        Optional<UserDeck> optionalDeck = deckRepository.findByIdAndUserId(deckId, ownerId);
        if (optionalDeck.isEmpty())
            return Optional.empty();

        UserDeck deck = optionalDeck.get();
        if (!Boolean.TRUE.equals(deck.getIsPublic()) && !ownerId.equals(viewerId))
            return Optional.empty();

        return Optional.of(buildDeckResponse(deck, getUser(ownerId)));
    }

    @Transactional(readOnly = true)
    public Optional<DeckResponse> getDeckForEdit(Integer userId, Integer deckId) {
        Optional<UserDeck> deckOpt = deckRepository.findByIdAndUserId(deckId, userId);
        if (deckOpt.isEmpty()) {
            System.out.println("找不到卡組或無權限編輯: deckId=" + deckId + ", userId=" + userId);
        }
        return deckOpt.map(deck -> buildDeckResponse(deck, getUser(userId)));
    }

    @Transactional(readOnly = true)
    public Resource loadDeckFileResource(Integer ownerId, Integer deckId, Integer viewerId) {
        UserDeck deck = deckRepository.findByIdAndUserId(deckId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("找不到卡組"));

        // 檢查檢視權限（若非公開且非本人，則拒絕）
        if (!Boolean.TRUE.equals(deck.getIsPublic()) && !ownerId.equals(viewerId)) {
            throw new SecurityException("無權限存取此卡組");
        }

        try {
            Path filePath = getDeckPath(ownerId, deck.getName());
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new IllegalStateException("伺服器上找不到對應的 YDK 檔案");
        } catch (Exception e) {
            throw new IllegalStateException("無法讀取 YDK 檔案", e);
        }
    }
    // ============================================================
    // Create
    // ============================================================

    @Transactional
    public String createDeckPack(DeckPackCreateRequest request) {
        String name = normalizeName(request.getName());
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("卡組 pack 名稱不可為空");
        if (name.length() > 80)
            throw new IllegalArgumentException("卡組 pack 名稱不可超過 80 個字元");
        if (deckPackRepository.existsByName(name))
            throw new IllegalArgumentException("卡組 pack 名稱已存在");
        return deckPackRepository.save(new DeckPack(name)).getName();
    }

    @Transactional
    public DeckResponse createDeck(Integer userId, UserDeckEditRequest request) {
        User user = getUser(userId);
        if (request.getDeckInfo() == null) {
            throw new IllegalArgumentException("卡組資訊不可為空");
        }

        String deckName = normalizeName(request.getDeckInfo().getName());
        validateName(deckName);
        String packName = validatePackName(request.getDeckInfo().getPackName());
        Boolean isPublic = request.getDeckInfo().getIsPublic();
        if (isPublic == null) {
            isPublic = false;
        }

        if (deckRepository.existsByUserIdAndNameIgnoreCase(userId, deckName)) {
            throw new IllegalArgumentException("卡組名稱已存在");
        }

        Path filePath = getDeckPath(userId, deckName);
        boolean copied = false;
        String contents = getValidateYdkContent(user, request);
        try {
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, contents);
            copied = true;

            UserDeck deck = UserDeck.builder()
                    .userId(userId)
                    .name(deckName)
                    .packName(packName)
                    .isPublic(isPublic)
                    .updatedAt(LocalDateTime.now())
                    .build();
            return DeckResponse.fromModel(deckRepository.save(deck), user);
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

    // ============================================================
    // Update
    // ============================================================

    @Transactional
    public DeckResponse editDeck(Integer userId, Integer deckId, UserDeckEditRequest request) {
        User user = getUser(userId);
        UserDeck deck = getOwnedDeck(userId, deckId);
        if (request.getDeckInfo() == null) {
            throw new IllegalArgumentException("卡組資訊不可為空");
        }

        String newName = normalizeName(request.getDeckInfo().getName());
        validateName(newName);
        String packName = validatePackName(request.getDeckInfo().getPackName());
        Boolean isPublic = request.getDeckInfo().getIsPublic();
        if (isPublic == null)
            throw new IllegalArgumentException("公開狀態不可為空");

        if (!deck.getName().equalsIgnoreCase(newName)
                && deckRepository.existsByUserIdAndNameIgnoreCase(userId, newName)) {
            throw new IllegalArgumentException("卡組名稱已存在");
        }

        Path oldPath = getDeckPath(userId, deck.getName());
        Path newPath = getDeckPath(userId, newName);
        boolean renamed = !oldPath.equals(newPath);
        if (renamed && Files.exists(newPath))
            throw new IllegalArgumentException("相同檔名的 YDK 檔案已存在");

        String contents = getValidateYdkContent(user, request);

        try {
            Files.createDirectories(newPath.getParent());
            Path temporaryPath = Files.createTempFile(newPath.getParent(), ".deck-edit-", ".tmp");
            try {
                Files.writeString(temporaryPath, contents);
                moveReplacing(temporaryPath, newPath);
            } finally {
                Files.deleteIfExists(temporaryPath);
            }

            deck.setName(newName);
            deck.setPackName(packName);
            deck.setIsPublic(isPublic);
            deck.setUpdatedAt(LocalDateTime.now());
            UserDeck saved = deckRepository.save(deck);
            if (renamed)
                Files.deleteIfExists(oldPath);
            return DeckResponse.fromModel(saved, user);
        } catch (IOException exception) {
            throw new IllegalStateException("無法儲存卡組內容", exception);
        }
    }

    // ============================================================
    // Delete
    // ============================================================

    @Transactional
    public void deleteDeck(Integer userId, Integer deckId) {
        UserDeck deck = getOwnedDeck(userId, deckId);
        Path filePath = getDeckPath(userId, deck.getName());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new IllegalStateException("無法刪除 YDK 檔案", exception);
        }
        deckRepository.delete(deck);
    }

    // ============================================================
    // Helpers / View
    // ============================================================

    private DeckResponse buildDeckResponse(UserDeck deck, User user) {
        DeckResponse response = DeckResponse.fromModel(deck, user);
        try {
            Path filePath = getDeckPath(deck.getUserId(), deck.getName());
            if (!Files.exists(filePath)) {
                return response;
            }
            List<String> lines = Files.readAllLines(filePath);
            boolean hasMain = lines.stream().anyMatch(line -> line.trim().equalsIgnoreCase("#main"));

            List<Integer> pack = new ArrayList<>();
            List<Integer> main = new ArrayList<>();
            List<Integer> extra = new ArrayList<>();
            List<Integer> side = new ArrayList<>();
            List<Integer> current = hasMain ? main : pack;

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

            response.setPack(pack);
            response.setMain(main);
            response.setExtra(extra);
            response.setSide(side);
        } catch (IOException ignored) {
        }
        return response;
    }

    private User getUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("找不到用戶"));
    }

    private UserDeck getOwnedDeck(Integer userId, Integer deckId) {
        return deckRepository.findByIdAndUserId(deckId, userId)
                .orElseThrow(() -> new IllegalArgumentException("找不到卡組"));
    }

    private Path getDeckPath(Integer userId, String deckName) {
        Path root = Path.of(deckStorageDirectory).toAbsolutePath().normalize();
        return resolveDeckPath(root, userId, deckName);
    }

    private Path resolveDeckPath(Path root, Integer userId, String deckName) {
        Path userDirectory = root.resolve(String.valueOf(userId)).normalize();
        Path filePath = userDirectory.resolve(deckName + ".ydk").normalize();
        if (!userDirectory.startsWith(root) || !filePath.startsWith(userDirectory)) {
            throw new IllegalArgumentException("卡組名稱無效");
        }
        return filePath;
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    private String validatePackName(String packName) {
        String normalized = normalizeName(packName);
        if (normalized == null || normalized.isBlank())
            return null;
        if (!deckPackRepository.existsByName(normalized)) {
            throw new IllegalArgumentException("找不到指定的卡組 pack");
        }
        return normalized;
    }

    private String getValidateYdkContent(User user, UserDeckEditRequest request) {
        List<Integer> pack = validatePacks(request.getPack(), 90, "展示卡組最多 90 張");
        List<Integer> main = validatePacks(request.getMain(), 60, "主要卡組最多 60 張");
        List<Integer> extra = validatePacks(request.getExtra(), 15, "額外卡組最多 15 張");
        List<Integer> side = validatePacks(request.getSide(), 15, "副卡組最多 15 張");

        Map<Integer, Integer> cardCounts = new HashMap<>();
        List<Integer> allCards = new ArrayList<>(pack.size() + main.size() + extra.size() + side.size());
        allCards.addAll(pack);
        allCards.addAll(main);
        allCards.addAll(extra);
        allCards.addAll(side);
        for (Integer cardId : allCards) {
            if (cardId == null)
                continue;
            cardCounts.merge(cardId, 1, Integer::sum);
        }

        for (Map.Entry<Integer, Integer> entry : cardCounts.entrySet())
            if (entry.getValue() > 3)
                throw new IllegalArgumentException("相同卡號最多只能投入 3 張: " + entry.getKey());

        StringBuilder content = new StringBuilder("$#created by {user.getName()}\n");
        if (pack != null && !pack.isEmpty()) {
            pack.forEach(id -> content.append(id).append('\n'));
        } else {
            content.append("#main\n");
            main.forEach(id -> content.append(id).append('\n'));
            content.append("#extra\n");
            extra.forEach(id -> content.append(id).append('\n'));
            content.append("!side\n");
            side.forEach(id -> content.append(id).append('\n'));
        }

        return content.toString();
    }

    private List<Integer> validatePacks(List<Integer> cardIds, int maxCount, String errMsg) {
        if (cardIds == null)
            return List.of();
        List<Integer> filteredIds = cardIds.stream().filter(id -> id != null && id > 0).toList();
        if (filteredIds.size() > maxCount)
            throw new IllegalArgumentException(errMsg);
        return filteredIds;
    }

    private void moveReplacing(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void validateName(String name) {
        if (name == null || !VALID_NAME.matcher(name).matches() || name.equals(".") || name.equals("..")
                || name.endsWith(".")) {
            throw new IllegalArgumentException("名稱限 1 至 80 個中英文、數字、空格、括號、底線或連字號");
        }
        if (name.matches("(?i)^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$")) {
            throw new IllegalArgumentException("此名稱無法作為檔案名稱");
        }
    }
}