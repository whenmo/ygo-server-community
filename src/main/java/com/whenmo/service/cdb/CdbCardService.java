package com.whenmo.service.cdb;

import org.springframework.stereotype.Service;

import com.whenmo.model.cdb.domain.CdbCard;
import com.whenmo.model.cdb.response.CdbCardResponse;
import com.whenmo.model.database.response.CdbConfigResponse;
import lombok.RequiredArgsConstructor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CdbCardService {
    private final CdbDiskCacheService diskCache;

    private static final String BASE_SELECT_SQL = "SELECT d.id, d.ot, d.alias, d.setcode, d.type, d.atk, d.def, "
            + "d.level, d.race, d.attribute, d.category, t.name, t.desc "
            + "FROM datas d LEFT JOIN texts t ON d.id = t.id";

    private String getUrl(CdbConfigResponse configResponse) {
        Objects.requireNonNull(configResponse, "CdbConfigResponse 不可為 null");
        if (configResponse.getFilePath() == null || configResponse.getFilePath().isBlank()) {
            throw new IllegalArgumentException("CDB 配置檔或檔案路徑不可為空");
        }
        return "jdbc:sqlite:" + configResponse.getFilePath();
    }

    private CdbCard mapRowCard(ResultSet resultSet) throws Exception {
        CdbCard card = new CdbCard();
        card.setId(resultSet.getInt("id"));
        card.setOt(resultSet.getObject("ot", Integer.class));
        card.setAlias(resultSet.getObject("alias", Integer.class));
        card.setSetcode(resultSet.getObject("setcode", Long.class));
        card.setType(resultSet.getObject("type", Long.class));
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

    /**
     * 獲取所有 CDB 的組合檔案
     * 當 CdbCardResponse.id 相同時，後讀取的會覆蓋先前讀取的
     */
    public List<CdbCardResponse> getAllCardsCombined(List<CdbConfigResponse> configResponses) {
        if (configResponses == null || configResponses.isEmpty()) {
            return new ArrayList<>();
        }

        List<CdbCardResponse> cachedCards = diskCache.getAllCdbCardCache(configResponses);
        if (cachedCards != null) {
            return cachedCards;
        }

        List<CdbCardResponse> allCards = new ArrayList<>();
        for (CdbConfigResponse config : configResponses) {
            allCards.addAll(readCdb(config, false));
        }
        List<CdbCardResponse> combined = combineCards(allCards);
        diskCache.putAllCdbCardCache(configResponses, combined);
        return combined;
    }

    private List<CdbCardResponse> combineCards(List<CdbCardResponse> cards) {
        Map<Integer, CdbCardResponse> cardMap = new java.util.LinkedHashMap<>();
        for (CdbCardResponse card : cards) {
            cardMap.put(card.getId(), card);
        }

        List<CdbCardResponse> combined = new ArrayList<>(cardMap.values());
        return combined;
    }

    /**
     * 獲取 CDB 檔中的所有卡片
     */
    public List<CdbCardResponse> readCdb(CdbConfigResponse configResponse, boolean forceRefresh) {
        if (forceRefresh) {
            diskCache.removeCdbCardCache(configResponse.getId());
        }
        if (!forceRefresh) {
            List<CdbCardResponse> cachedCards = diskCache.getCdbCardCache(configResponse);
            if (cachedCards != null)
                return cachedCards;
        }

        try (Connection connection = DriverManager.getConnection(getUrl(configResponse));
                PreparedStatement statement = connection.prepareStatement(BASE_SELECT_SQL);
                ResultSet resultSet = statement.executeQuery()) {
            List<CdbCardResponse> cards = new ArrayList<>();
            while (resultSet.next()) {
                CdbCard card = mapRowCard(resultSet);
                cards.add(CdbCardResponse.fromModel(card, configResponse));
            }
            diskCache.putCdbCardCache(configResponse, cards);
            return cards;
        } catch (Exception e) {
            throw new RuntimeException("讀取 configId " + configResponse.getId() + " 的 CDB 檔案失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 根據卡片 ID 查詢單張卡片
     */
    public CdbCardResponse getCardById(CdbConfigResponse configResponse, int id) {
        Objects.requireNonNull(id, "卡片 ID 不可為 null");

        String sql = BASE_SELECT_SQL + " WHERE d.id = ?";

        try (Connection connection = DriverManager.getConnection(getUrl(configResponse));
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            CdbCardResponse cardResponse = null;
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    CdbCard card = mapRowCard(resultSet);
                    cardResponse = CdbCardResponse.fromModel(card, configResponse);
                }
            }
            return cardResponse;
        } catch (Exception e) {
            throw new RuntimeException("讀取 configId " + configResponse.getId() + " 的 CDB 檔案失敗: " + e.getMessage(), e);
        }
    }
}
