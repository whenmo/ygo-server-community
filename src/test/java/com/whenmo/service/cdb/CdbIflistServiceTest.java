package com.whenmo.service.cdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.whenmo.model.cardinfo.IflistItem;
import com.whenmo.model.database.response.CdbConfigResponse;

class CdbIflistServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void parsesSpacedVersionAndDropsCommentsMalformedRowsAndInvalidStatuses() throws IOException {
        Path lflist = tempDir.resolve("lflist.conf");
        Files.writeString(lflist, """
                !Previous version
                100 0 -- previous card
                !2026.7.9 44023DIY
                # comment row
                2001 0 -- forbidden
                2002 1 -- limited
                2003 2 -- semi-limited
                2004 3 -- invalid status
                invalid-id 1 -- invalid card ID
                2005 invalid -- invalid status
                !Next version
                2006 1 -- next card
                """, StandardCharsets.UTF_8);

        CdbConfigResponse config = CdbConfigResponse.builder()
                .id(2)
                .filePath(tempDir.resolve("cards.cdb").toString())
                .build();
        CdbIflistService service = new CdbIflistService();

        assertTrue(service.getAllVersions(config).contains("2026.7.9 44023DIY"));
        assertEquals(List.of(new IflistItem(2001, 0)), service.getBanlistByVersion(config, "2026.7.9 44023DIY").get(0));
        assertEquals(List.of(new IflistItem(2002, 1)), service.getBanlistByVersion(config, "2026.7.9 44023DIY").get(1));
        assertEquals(List.of(new IflistItem(2003, 2)), service.getBanlistByVersion(config, "2026.7.9 44023DIY").get(2));
        assertEquals(-1, service.getCardStatus(config, "2026.7.9 44023DIY", 2004));
        assertEquals(-1, service.getCardStatus(config, "2026.7.9 44023DIY", 2006));
        assertEquals(1, service.getCardStatus(config, "Next version", 2006));
    }
}