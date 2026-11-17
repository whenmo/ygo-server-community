package com.whenmo.service.database;

import com.whenmo.model.database.dto.YdkDto;
import com.whenmo.model.database.domain.Deck;
import com.whenmo.model.database.request.DeckEditRequest;
import com.whenmo.model.database.response.DeckInfoResponse;
import com.whenmo.model.database.response.DeckResponse;
import com.whenmo.repository.database.DeckPackRepository;
import com.whenmo.repository.database.DeckRepository;
import com.whenmo.service.RestServiceHelper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeckRestService {

    private final DeckRepository deckRepository;
    private final UserRestService userRestService;
    private final DiskDecksService diskDecksService;
    private final DeckPackRepository deckPackRepository;

    // ============================================================
    // Read
    // ============================================================

    @Transactional(readOnly = true)
    public List<DeckInfoResponse> getPublicDecks() {
        return deckRepository.findPublicDecks().stream().map(DeckInfoResponse::fromDeck).toList();
    }

    @Transactional(readOnly = true)
    public List<DeckInfoResponse> getUserDecks(Integer userId) {
        return deckRepository.findByUserIdOrderByIsFavorDescUpdatedAtDesc(userId).stream()
                .map(DeckInfoResponse::fromDeck).toList();
    }

    @Transactional(readOnly = true)
    public DeckResponse getPublicDeck(Integer deckId) {
        Deck deck = findPublicDeckById(deckId);
        Map<String, List<Integer>> deckMap = diskDecksService.parseYdkContent(deckId);
        return DeckResponse.fromDeckAndMap(deck, deckMap.get("pack"), deckMap.get("main"), deckMap.get("extra"),
                deckMap.get("side"));
    }

    @Transactional(readOnly = true)
    public DeckResponse getUserDeck(Integer userId, Integer deckId) {
        Deck deck = findByIdAndUserId(deckId, userId);
        Map<String, List<Integer>> deckMap = diskDecksService.parseYdkContent(deckId);
        return DeckResponse.fromDeckAndMap(deck, deckMap.get("pack"), deckMap.get("main"), deckMap.get("extra"),
                deckMap.get("side"));
    }

    @Transactional(readOnly = true)
    public YdkDto getPublicDeckYdkResource(Integer deckId) {
        Deck deck = findPublicDeckById(deckId);
        return new YdkDto(deck.getName(), diskDecksService.getYdkResource(deckId));
    }

    @Transactional(readOnly = true)
    public YdkDto getUserDeckYdkResource(Integer userId, Integer deckId) {
        Deck deck = findByIdAndUserId(deckId, userId);
        return new YdkDto(deck.getName(), diskDecksService.getYdkResource(deckId));
    }

    // ============================================================
    // Create
    // ============================================================

    @Transactional
    public DeckResponse createDeck(Integer userId, DeckEditRequest request) {
        String name = RestServiceHelper.getValidTrimString(request.getName(), "卡組名稱不可為空");

        Integer packId = getValidPackId(request.getPackId(), userId);

        Deck deck = deckRepository.save(request.toDeck(name, userId, packId));

        diskDecksService.saveYdkFile(deck.getId(), userRestService.getNameById(userId), request);

        Map<String, List<Integer>> deckMap = diskDecksService.parseYdkContent(deck.getId());
        return DeckResponse.fromDeckAndMap(deck, deckMap.get("pack"), deckMap.get("main"), deckMap.get("extra"),
                deckMap.get("side"));
    }

    // ============================================================
    // Update
    // ============================================================

    @Transactional
    public DeckResponse updateDeck(Integer userId, Integer deckId, DeckEditRequest request) {
        String name = RestServiceHelper.getValidTrimString(request.getName(), "卡組名稱不可為空");

        Deck deck = findByIdAndUserId(deckId, userId);

        Integer packId = getValidPackId(request.getPackId(), userId);

        request.updateDeck(deck, name, packId);

        deck = deckRepository.save(deck);

        diskDecksService.saveYdkFile(deck.getId(), userRestService.getNameById(userId), request);

        Map<String, List<Integer>> deckMap = diskDecksService.parseYdkContent(deck.getId());
        return DeckResponse.fromDeckAndMap(deck, deckMap.get("pack"), deckMap.get("main"), deckMap.get("extra"),
                deckMap.get("side"));
    }

    // ============================================================
    // Delete
    // ============================================================

    @Transactional
    public void deleteDeck(Integer userId, Integer deckId) {
        deckRepository.delete(findByIdAndUserId(deckId, userId));
        diskDecksService.deleteYdkFile(deckId);
    }

    // ============================================================
    // Helpers / View
    // ============================================================

    /**
     * 依據 deck_id 與 user_id 查詢單個卡組
     */
    private Deck findByIdAndUserId(Integer deckId, Integer userId) {
        return deckRepository.findByIdAndUserId(deckId, userId)
                .orElseThrow(() -> new IllegalArgumentException("找不到用戶卡組_" + userId + "_" + deckId));
    }

    /**
     * 依據 deck_id 查詢公開卡組
     */
    private Deck findPublicDeckById(Integer deckId) {
        return deckRepository.findPublicDeckById(deckId)
                .orElseThrow(() -> new IllegalArgumentException("找不到公開卡組_" + deckId));
    }

    /**
     * 檢查用戶可否使用 packId 卡包
     */
    private Integer getValidPackId(Integer packId, Integer userId) {
        if (packId == null)
            return null;
        deckPackRepository.findByIdAndUserIdOrUserIdIsNull(packId, userId)
                .orElseThrow(() -> new IllegalArgumentException("找不到用戶/公開卡包_" + userId + "_" + packId));
        return packId;
    }
}