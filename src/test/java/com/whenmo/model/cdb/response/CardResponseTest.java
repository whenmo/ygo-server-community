package com.whenmo.model.cdb.response;

import com.whenmo.model.cdb.domain.Card;
import com.whenmo.model.database.response.CdbConfigResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CardResponseTest {

    @Test
    void replacesCardIdInMainAndBackupCdnTemplates() {
        Card card = new Card();
        card.setId(12345);
        CdbConfigResponse cdbConfig = CdbConfigResponse.builder()
                .id(7)
                .picCdn("https://cdn.example/cards/__id__.jpg")
                .backupPicCdns(List.of(
                        "https://backup.example/cards/__id__.webp",
                        "https://backup.example/cards/static.jpg",
                        "https://mirror.example/__id__/__id__.png"))
                .build();

        CardResponse response = CardResponse.fromCard(card, cdbConfig);

        assertEquals("https://cdn.example/cards/12345.jpg", response.getPicCdn());
        assertEquals(List.of(
                "https://backup.example/cards/12345.webp",
                "https://mirror.example/12345/12345.png"), response.getBackupPicCdns());
    }

    @Test
    void returnsNullForMainCdnAndDropsBackupsWithoutIdToken() {
        Card card = new Card();
        card.setId(12345);
        CdbConfigResponse cdbConfig = CdbConfigResponse.builder()
                .id(7)
                .picCdn("https://cdn.example/cards/static.jpg")
                .backupPicCdns(List.of("", "https://backup.example/cards/static.jpg"))
                .build();

        CardResponse response = CardResponse.fromCard(card, cdbConfig);

        assertNull(response.getPicCdn());
        assertEquals(List.of(), response.getBackupPicCdns());
    }
}