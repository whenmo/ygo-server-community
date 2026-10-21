package com.whenmo.service.cdb;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.whenmo.model.cdb.CdbCard;
import com.whenmo.model.database.response.CdbConfigResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CdbPicService {

    /**
     * 根據 CDB 配置與卡片 ID 批次搜尋卡圖 Resource
     */
    public List<Resource> getPicResources(CdbConfigResponse config, Integer... cardIds) {
        Objects.requireNonNull(config, "CdbConfigResponse 不可為 null");
        List<Resource> resources = new ArrayList<>();

        if (config.getFilePath() == null || config.getFilePath().isBlank()) {
            return resources;
        }

        Path picsPath = Path.of(config.getFilePath()).toAbsolutePath().normalize().getParent().resolve("pics");
        if (!Files.isDirectory(picsPath)) {
            return resources;
        }

        for (Integer cardId : cardIds) {
            if (!CdbCard.isValidId(cardId)) {
                continue;
            }

            Path image = picsPath.resolve(cardId + ".jpg").normalize();
            if (image.startsWith(picsPath) && Files.isRegularFile(image)) {
                resources.add(new FileSystemResource(image));
            }
        }

        return resources;
    }
}