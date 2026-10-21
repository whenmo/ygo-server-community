package com.whenmo.service.database;

import com.whenmo.model.database.domain.User;
import com.whenmo.model.database.domain.DeckPack;
import com.whenmo.model.database.domain.UserDeck;
import com.whenmo.model.cardinfo.CardType;
import com.whenmo.model.cardinfo.IflistItem;
import com.whenmo.model.cdb.CdbCardResponse;
import com.whenmo.model.database.response.CdbConfigResponse;
import com.whenmo.model.database.request.DeckPackCreateRequest;
import com.whenmo.model.database.request.UserDeckEditRequest;
import com.whenmo.model.database.request.UserDeckUploadRequest;
import com.whenmo.model.database.request.UserDeckVisibilityRequest;
import com.whenmo.model.database.response.UserDeckResponse;
import com.whenmo.model.database.response.UserDeckViewResponse;
import com.whenmo.repository.database.UserDeckRepository;
import com.whenmo.repository.database.DeckPackRepository;
import com.whenmo.repository.database.UserRepository;
import com.whenmo.service.cdb.CdbCardService;
import com.whenmo.service.cdb.CdbIflistService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final CdbConfigRestService cdbConfigService;
    private final CdbCardService cdbCardService;
    private final CdbIflistService cdbIflistService;

    @Value("${app.user-decks.storage-dir:../decks}")
    private String deckStorageDirectory;

    @Transactional(readOnly = true)
    public List<UserDeckResponse> getUserDecks(Integer userId) {
        String userName = getUser(userId).getName();
        return deckRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(deck -> UserDeckResponse.fromModel(deck, userName))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserDeckResponse> getPublicDecks() {
        Map<Integer, String> userNames = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, User::getName));
        return deckRepository.findByIsPublicTrueOrderByUpdatedAtDesc().stream()
                .map(deck -> UserDeckResponse.fromModel(deck, userNames.getOrDefault(deck.getUserId(), "未知用戶")))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getDeckPacks() {
        return deckPackRepository.findAll().stream()
                .map(DeckPack::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @Transactional
    public String createDeckPack(DeckPackCreateRequest request) {
        String name = normalizeName(request.getName());
        if (name == null || name.isBlank()) throw new IllegalArgumentException("卡組 pack 名稱不可為空");
        if (name.length() > 80) throw new IllegalArgumentException("卡組 pack 名稱不可超過 80 個字元");
        if (deckPackRepository.existsByName(name)) throw new IllegalArgumentException("卡組 pack 名稱已存在");
        return deckPackRepository.save(new DeckPack(name)).getName();
    }

    @Transactional
    public UserDeckResponse uploadDeck(Integer userId, UserDeckUploadRequest request, MultipartFile file) {
        getUser(userId);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("請選擇 YDK 檔案");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase(Locale.ROOT).endsWith(".ydk")) {
            throw new IllegalArgumentException("只接受 YDK 檔案");
        }

        String deckName = normalizeName(request.getName());
        if (deckName == null || deckName.isBlank()) {
            String fileName = originalName.replace('\\', '/');
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
            deckName = normalizeName(fileName.substring(0, fileName.length() - 4));
        }
        validateName(deckName);
        String packName = validatePackName(request.getPackName());
        if (deckRepository.existsByUserIdAndNameIgnoreCase(userId, deckName)) {
            throw new IllegalArgumentException("卡組名稱已存在");
        }

        Path filePath = getDeckPath(userId, deckName);
        boolean copied = false;
        try {
            Files.createDirectories(filePath.getParent());
            Files.copy(file.getInputStream(), filePath);
            copied = true;

            UserDeck deck = UserDeck.builder()
                    .userId(userId)
                    .name(deckName)
                    .packName(packName)
                    .isPublic(Boolean.TRUE.equals(request.getIsPublic()))
                    .updatedAt(LocalDateTime.now())
                    .build();
            return UserDeckResponse.fromModel(deckRepository.save(deck), getUser(userId).getName());
        } catch (IOException | RuntimeException exception) {
            if (copied) {
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException ignored) {
                    exception.addSuppressed(ignored);
                }
            }
            if (exception instanceof RuntimeException runtimeException) throw runtimeException;
            throw new IllegalStateException("無法儲存 YDK 檔案", exception);
        }
    }

    @Transactional
    public UserDeckResponse editDeck(Integer userId, Integer deckId, UserDeckEditRequest request) {
        UserDeck deck = getOwnedDeck(userId, deckId);
        String newName = normalizeName(request.getName());
        validateName(newName);
        String packName = validatePackName(request.getPackName());
        if (request.getIsPublic() == null) throw new IllegalArgumentException("公開狀態不可為空");

        if (!deck.getName().equalsIgnoreCase(newName)
                && deckRepository.existsByUserIdAndNameIgnoreCase(userId, newName)) {
            throw new IllegalArgumentException("卡組名稱已存在");
        }

        List<Integer> main = validateCardIds(request.getMainDeck());
        List<Integer> extra = validateCardIds(request.getExtraDeck());
        List<Integer> side = validateCardIds(request.getSideDeck());
        validateDeckRules(request.getCdbId(), request.getIflistVersion(), main, extra, side);
        String contents = toYdk(main, extra, side);

        Path oldPath = getDeckPath(userId, deck.getName());
        Path newPath = getDeckPath(userId, newName);
        boolean renamed = !oldPath.equals(newPath);
        if (renamed && Files.exists(newPath)) throw new IllegalArgumentException("相同檔名的 YDK 檔案已存在");

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
            deck.setIsPublic(request.getIsPublic());
            deck.setUpdatedAt(LocalDateTime.now());
            UserDeck saved = deckRepository.save(deck);
            if (renamed) Files.deleteIfExists(oldPath);
            return UserDeckResponse.fromModel(saved, getUser(userId).getName());
        } catch (IOException exception) {
            throw new IllegalStateException("無法儲存卡組內容", exception);
        }
    }

    @Transactional
    public UserDeckResponse updateVisibility(Integer userId, Integer deckId, UserDeckVisibilityRequest request) {
        if (request.getIsPublic() == null) {
            throw new IllegalArgumentException("公開狀態不可為空");
        }
        UserDeck deck = getOwnedDeck(userId, deckId);
        deck.setIsPublic(request.getIsPublic());
        deck.setUpdatedAt(LocalDateTime.now());
        return UserDeckResponse.fromModel(deckRepository.save(deck), getUser(userId).getName());
    }

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

    @Transactional(readOnly = true)
    public Optional<UserDeckViewResponse> getDeckForView(Integer ownerId, Integer deckId, Integer viewerId) {
        Optional<UserDeck> optionalDeck = deckRepository.findByIdAndUserId(deckId, ownerId);
        if (optionalDeck.isEmpty()) return Optional.empty();

        UserDeck deck = optionalDeck.get();
        if (!Boolean.TRUE.equals(deck.getIsPublic()) && !ownerId.equals(viewerId)) return Optional.empty();

        return Optional.of(toViewResponse(ownerId, deck));
    }

    @Transactional(readOnly = true)
    public Optional<UserDeckViewResponse> getDeckForEdit(Integer userId, Integer deckId) {
        return deckRepository.findByIdAndUserId(deckId, userId)
                .map(deck -> toViewResponse(userId, deck));
    }

    private UserDeckViewResponse toViewResponse(Integer ownerId, UserDeck deck) {
        UserDeckResponse deckResponse = UserDeckResponse.fromModel(deck, getUser(ownerId).getName());
        try {
            List<Integer> main = new ArrayList<>();
            List<Integer> extra = new ArrayList<>();
            List<Integer> side = new ArrayList<>();
            List<Integer> current = main;

            for (String line : Files.readAllLines(getDeckPath(ownerId, deck.getName()))) {
                String value = line.trim();
                if (value.equalsIgnoreCase("#extra")) {
                    current = extra;
                } else if (value.equalsIgnoreCase("!side")) {
                    current = side;
                } else if (value.matches("\\d+")) {
                    current.add(Integer.parseInt(value));
                }
            }

                return UserDeckViewResponse.builder()
                    .deck(deckResponse)
                    .mainDeck(main)
                    .extraDeck(extra)
                    .sideDeck(side)
                    .build();
        } catch (IOException exception) {
            throw new IllegalStateException("無法讀取 YDK 檔案", exception);
        }
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
        if (normalized == null || normalized.isBlank()) return null;
        if (!deckPackRepository.existsByName(normalized)) {
            throw new IllegalArgumentException("找不到指定的卡組 pack");
        }
        return normalized;
    }

    private List<Integer> validateCardIds(List<Integer> cardIds) {
        if (cardIds == null) return List.of();
        if (cardIds.size() > 80) throw new IllegalArgumentException("單一牌組區最多只能有 80 張卡");
        if (cardIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("卡片 ID 無效");
        }
        return List.copyOf(cardIds);
    }

    private void validateDeckRules(Integer cdbId, String iflistVersion,
            List<Integer> main, List<Integer> extra, List<Integer> side) {
        if (main.size() > 60) throw new IllegalArgumentException("主要卡組最多 60 張");
        if (extra.size() > 15) throw new IllegalArgumentException("額外卡組最多 15 張");
        if (side.size() > 15) throw new IllegalArgumentException("Side 最多 15 張");
        if (cdbId == null) throw new IllegalArgumentException("請先選擇卡片資料庫");

        CdbConfigResponse config = cdbConfigService.getConfigById(cdbId);
        Map<Integer, CdbCardResponse> cardsById = cdbCardService.readCdb(config).stream()
                .collect(Collectors.toMap(CdbCardResponse::getId, Function.identity(), (first, latest) -> latest));

        long extraMask = CardType.FUSION.getCode() | CardType.SYNCHRO.getCode()
                | CardType.XYZ.getCode() | CardType.LINK.getCode();
        for (Integer cardId : main) {
            CdbCardResponse card = cardsById.get(cardId);
            if (card != null && (card.getType() & extraMask) != 0) {
                throw new IllegalArgumentException("額外牌組卡不能放入主要卡組: " + card.getName());
            }
        }
        for (Integer cardId : extra) {
            CdbCardResponse card = cardsById.get(cardId);
            if (card != null && (card.getType() & extraMask) == 0) {
                throw new IllegalArgumentException("此卡不能放入額外卡組: " + card.getName());
            }
        }

        Map<Integer, List<IflistItem>> banlist = iflistVersion == null || iflistVersion.isBlank()
                ? Map.of()
                : cdbIflistService.getBanlistByVersion(config, iflistVersion);
        Map<Integer, Integer> copyCounts = new HashMap<>();
        Map<Integer, Integer> copyLimits = new HashMap<>();

        for (Integer cardId : concat(main, extra, side)) {
            CdbCardResponse card = cardsById.get(cardId);
            int copyKey = card != null && card.getAlias() > 0 ? card.getAlias() : cardId;
            copyCounts.merge(copyKey, 1, Integer::sum);
            int limit = card == null ? 3 : getBanlistLimit(card, banlist);
            copyLimits.merge(copyKey, limit, Math::min);
        }

        for (Map.Entry<Integer, Integer> entry : copyCounts.entrySet()) {
            int maximum = copyLimits.getOrDefault(entry.getKey(), 3);
            if (entry.getValue() > maximum) {
                throw new IllegalArgumentException(maximum == 0
                        ? "卡組包含禁止投入的卡片: " + entry.getKey()
                        : "同卡（含 alias）最多只能投入 " + maximum + " 張: " + entry.getKey());
            }
        }
    }

    private int getBanlistLimit(CdbCardResponse card, Map<Integer, List<IflistItem>> banlist) {
        int alias = card.getAlias();
        for (Map.Entry<Integer, List<IflistItem>> entry : banlist.entrySet()) {
            if (entry.getValue().stream().anyMatch(item -> item.getId() == card.getId()
                    || (alias > 0 && item.getId() == alias))) {
                return entry.getKey();
            }
        }
        return 3;
    }

    private List<Integer> concat(List<Integer> main, List<Integer> extra, List<Integer> side) {
        List<Integer> all = new ArrayList<>(main.size() + extra.size() + side.size());
        all.addAll(main);
        all.addAll(extra);
        all.addAll(side);
        return all;
    }

    private String toYdk(List<Integer> main, List<Integer> extra, List<Integer> side) {
        StringBuilder content = new StringBuilder("#main\n");
        main.forEach(id -> content.append(id).append('\n'));
        content.append("#extra\n");
        extra.forEach(id -> content.append(id).append('\n'));
        content.append("!side\n");
        side.forEach(id -> content.append(id).append('\n'));
        return content.toString();
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