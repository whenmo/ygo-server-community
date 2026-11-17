package com.whenmo.service.cdb;

import org.springframework.stereotype.Service;

import com.whenmo.model.cdb.domain.Card;
import com.whenmo.model.cdb.response.CardResponse;
import com.whenmo.model.database.response.CdbConfigResponse;
import com.whenmo.service.database.CdbConfigRestService;

import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CardService {
    private final DiskCacheService diskCache;
    private final CdbConfigRestService cdbConfigRestService;

    private static final String GET_ALL_SQL = "SELECT d.id, d.ot, d.alias, d.setcode, d.type, d.atk, d.def, "
            + "d.level, d.race, d.attribute, d.category, t.name, t.desc "
            + "FROM datas d LEFT JOIN texts t ON d.id = t.id";

    private static final String GET_ID_SQL = GET_ALL_SQL + " WHERE d.id = ?";

    // ============================================================
    // Read
    // ============================================================

    /**
     * 根據 cdbId 獲取 CDB 中的所有卡片
     */
    public List<CardResponse> getCdbCard(Integer cdbId) {
        CdbConfigResponse cdbConfig = cdbConfigRestService.getCdbConfig(cdbId);

        List<CardResponse> cachedCards = diskCache.getCdbCardCache(cdbConfig);
        if (cachedCards != null)
            return cachedCards;

        List<CardResponse> cards = getCdbCardsResponse(cdbConfig);

        diskCache.saveCdbCardCache(cdbConfig, cards);
        return cards;
    }

    /**
     * 根據 cdbId 與 cardId 查詢單張卡片
     */
    public CardResponse getCdbCardById(Integer cdbId, Integer cardId) {
        CdbConfigResponse cdbConfig = cdbConfigRestService.getCdbConfig(cdbId);

        return getCardsByIdResponse(cdbConfig, cardId);
    }

    /**
     * 根據 cardId 查詢單張卡片
     */
    public CardResponse getCardById(Integer cardId) {
        List<CdbConfigResponse> cdbConfigs = cdbConfigRestService.getAllConfigs();
        if (cdbConfigs.isEmpty())
            return null;

        for (CdbConfigResponse config : cdbConfigs) {
            CardResponse card = getCardsByIdResponse(config, cardId);
            if (card != null)
                return card;
        }
        return null;
    }

    /**
     * 獲取所有 CDB 中的所有卡片
     * 當 card id 相同時，後讀取的會覆蓋先前讀取的
     */
    public List<CardResponse> getCombineCdbCard() {
        List<CdbConfigResponse> cdbConfigs = cdbConfigRestService.getAllConfigs();
        if (cdbConfigs.isEmpty())
            return new ArrayList<>();

        List<CardResponse> cachedCards = diskCache.getCombineCdbCardCache(cdbConfigs);
        if (cachedCards != null)
            return cachedCards;

        Map<Integer, CardResponse> cardMap = new java.util.LinkedHashMap<>();
        for (CdbConfigResponse config : cdbConfigs)
            putAllCardResponse(config, cardMap);

        List<CardResponse> allCards = new ArrayList<>(cardMap.values());

        diskCache.saveCombineCdbCardCache(cdbConfigs, allCards);
        return allCards;
    }

    // ============================================================
    // Update
    // ============================================================

    /**
     * 刷新 cdbId 的 cache
     */
    public void refreshCdbCard(Integer cdbId) {
        diskCache.removeCdbCardCache(cdbId);
        getCdbCard(cdbId);
    }

    // ============================================================
    // Helpers
    // ============================================================

    private List<CardResponse> getCdbCardsResponse(CdbConfigResponse cdbConfig) {

        List<CardResponse> cards = new ArrayList<>();
        Integer cdbId = cdbConfig.getId();

        try (PreparedStatement statement = getStatement(cdbConfig, GET_ALL_SQL);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Card card = mapRowCard(resultSet);
                cards.add(CardResponse.fromCard(card, cdbId, cdbConfig.getPicCdn()));
            }
        } catch (Exception e) {
            throw new RuntimeException("讀取 cdbId " + cdbId + " 的 CDB 檔案失敗: " + e.getMessage(), e);
        }

        return cards;
    }

    private CardResponse getCardsByIdResponse(CdbConfigResponse cdbConfig, Integer cardId) {

        Integer cdbId = cdbConfig.getId();

        try (PreparedStatement statement = getStatement(cdbConfig, GET_ID_SQL)) {

            statement.setInt(1, cardId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Card card = mapRowCard(resultSet);
                    return CardResponse.fromCard(card, cdbId, cdbConfig.getPicCdn());
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("讀取 cdbId " + cdbId + " 的 CDB 檔案失敗: " + e.getMessage(), e);
        }
    }

    private void putAllCardResponse(CdbConfigResponse cdbConfig, Map<Integer, CardResponse> cardMap) {

        Integer cdbId = cdbConfig.getId();

        try (PreparedStatement statement = getStatement(cdbConfig, GET_ALL_SQL);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                CardResponse card = CardResponse.fromCard(mapRowCard(resultSet), cdbId, cdbConfig.getPicCdn());
                cardMap.put(card.getId(), card);
            }
        } catch (Exception e) {
            throw new RuntimeException("讀取 cdbId " + cdbId + " 的 CDB 檔案失敗: " + e.getMessage(), e);
        }
    }

    private PreparedStatement getStatement(CdbConfigResponse cdbConfig, String sql) throws SQLException {

        String filePath = cdbConfig.getFilePath();
        if (filePath == null || filePath.isBlank())
            throw new IllegalArgumentException("cdb 路徑不可為空");

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + cdbConfig.getFilePath());
        return connection.prepareStatement(sql);
    }

    private Card mapRowCard(ResultSet resultSet) throws Exception {
        Card card = new Card();
        card.setId(resultSet.getObject("id", Integer.class));
        card.setOt(resultSet.getObject("ot", Integer.class));
        card.setAlias(resultSet.getObject("alias", Integer.class));
        card.setSetcode(resultSet.getObject("setcode", Long.class));
        card.setType(resultSet.getObject("type", Integer.class));
        card.setAtk(resultSet.getObject("atk", Integer.class));
        card.setDef(resultSet.getObject("def", Integer.class));
        card.setLevel(resultSet.getObject("level", Integer.class));
        card.setRace(resultSet.getObject("race", Integer.class));
        card.setAttribute(resultSet.getObject("attribute", Integer.class));
        card.setCategory(resultSet.getObject("category", Long.class));
        card.setName(resultSet.getString("name"));
        card.setDesc(resultSet.getString("desc"));
        return card;
    }
}