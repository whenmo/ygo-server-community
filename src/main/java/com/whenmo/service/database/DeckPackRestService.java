package com.whenmo.service.database;

import com.whenmo.model.database.domain.DeckPack;
import com.whenmo.model.database.request.DeckPackEditRequest;
import com.whenmo.model.database.response.DeckPackResponse;
import com.whenmo.repository.database.DeckPackRepository;
import com.whenmo.service.RestServiceHelper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeckPackRestService {

    private final DeckPackRepository deckPackRepository;

    // ============================================================
    // Read
    // ============================================================

    @Transactional(readOnly = true)
    public List<DeckPackResponse> getPublicDeckPacks() {
        return deckPackRepository.findPublicDeckPacks().stream().map(DeckPackResponse::fromDeckPack).toList();
    }

    @Transactional(readOnly = true)
    public List<DeckPackResponse> getDeckPacksByUserId(Integer userId) {
        return deckPackRepository.findByUserId(userId).stream().map(DeckPackResponse::fromDeckPack).toList();
    }

    // ============================================================
    // Create
    // ============================================================

    @Transactional
    public DeckPackResponse createPublicDeckPack(DeckPackEditRequest request) {
        String name = RestServiceHelper.getValidTrimString(request.getName(), "公用卡包名稱不可為空");

        if (deckPackRepository.existsByNameAndUserIsNull(name)) {
            throw new IllegalArgumentException("公用卡包名稱已存在");
        }

        resolveNonPublicDeckPackConflict(name);

        DeckPack deckPack = request.toDeckPack(name, null);

        return DeckPackResponse.fromDeckPack(deckPackRepository.save(deckPack));
    }

    @Transactional
    public DeckPackResponse createPrivateDeckPack(Integer userId, DeckPackEditRequest request) {
        String name = RestServiceHelper.getValidTrimString(request.getName(), "卡包名稱不可為空");

        if (deckPackRepository.existsByNameAndUserIsNullOrEqualToUser(name, userId)) {
            throw new IllegalArgumentException("卡包名稱已存在");
        }

        DeckPack deckPack = request.toDeckPack(name, userId);

        return DeckPackResponse.fromDeckPack(deckPackRepository.save(deckPack));
    }

    // ============================================================
    // Update
    // ============================================================

    @Transactional
    public DeckPackResponse updatePublicDeckPack(Integer packId, DeckPackEditRequest request) {
        String name = RestServiceHelper.getValidTrimString(request.getName(), "卡包名稱不可為空");

        DeckPack deckPack = getPublicDeckById(packId);
        if (deckPack.getName().equals(name)) {
            throw new IllegalArgumentException("新舊卡包名稱相同");
        } else if (deckPackRepository.existsByNameAndUserIsNull(name)) {
            throw new IllegalArgumentException("卡包名稱已存在");
        }

        resolveNonPublicDeckPackConflict(name);

        request.updateDeckPack(deckPack, name);

        return DeckPackResponse.fromDeckPack(deckPackRepository.save(deckPack));
    }

    @Transactional
    public DeckPackResponse updatePrivateDeckPack(Integer packId, Integer userId, DeckPackEditRequest request) {
        String name = RestServiceHelper.getValidTrimString(request.getName(), "卡包名稱不可為空");

        DeckPack deckPack = findByIdAndUserId(packId, userId);
        if (deckPack.getName().equals(name)) {
            throw new IllegalArgumentException("新舊卡包名稱相同");
        } else if (deckPackRepository.existsByNameAndUserIsNullOrEqualToUser(name, userId)) {
            throw new IllegalArgumentException("卡包名稱已存在");
        }

        request.updateDeckPack(deckPack, name);

        return DeckPackResponse.fromDeckPack(deckPackRepository.save(deckPack));
    }

    // ============================================================
    // Delete
    // ============================================================

    @Transactional
    public void deletePublicDeckPack(Integer packId) {
        deckPackRepository.delete(getPublicDeckById(packId));
    }

    @Transactional
    public void deletePrivateDeckPack(Integer packId, Integer userId) {
        deckPackRepository.delete(findByIdAndUserId(packId, userId));
    }

    // ============================================================
    // Helper 輔助函數區塊
    // ============================================================

    /**
     * 依據 packId 與 user_id 查詢單個卡包
     */
    private DeckPack findByIdAndUserId(Integer packId, Integer userId) {
        return deckPackRepository.findByIdAndUserIdOrUserIdIsNull(packId, userId)
                .orElseThrow(() -> new IllegalArgumentException("找不到用戶/公開卡包_" + userId + "_" + packId));
    }

    /**
     * 查詢指定 packId 的單個公開卡包 (user_id 為 NULL)
     */
    public DeckPack getPublicDeckById(Integer packId) {
        return deckPackRepository.findPublicDeckPackById(packId)
                .orElseThrow(() -> new IllegalArgumentException("找不到公開卡包_" + packId));
    }

    /**
     * 檢查並解決非公開卡包重名衝突
     */
    private void resolveNonPublicDeckPackConflict(String name) {
        deckPackRepository.findPrivateDeckPacks().forEach(privatePack -> {
            Integer userId = privatePack.getUserId();
            String newName = name;
            int i = 1;
            while (deckPackRepository.existsByNameAndUserIsNullOrEqualToUser(newName + "_" + i, userId)) {
                i++;
            }
            privatePack.setName(newName + "_" + i);
            deckPackRepository.save(privatePack);
        });
    }
}