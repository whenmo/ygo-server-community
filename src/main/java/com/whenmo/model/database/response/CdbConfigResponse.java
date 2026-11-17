package com.whenmo.model.database.response;

import com.whenmo.model.database.domain.CdbConfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CdbConfigResponse {

    private Integer id;
    private String name;
    private String filePath;
    private String picCdn;

    public static CdbConfigResponse fromCdbConfig(CdbConfig cdbConfig) {
        if (cdbConfig == null)
            return null;

        return CdbConfigResponse.builder()
                .id(cdbConfig.getId())
                .name(cdbConfig.getName())
                .filePath(cdbConfig.getFilePath())
                .picCdn(cdbConfig.getPicCdn())
                .build();
    }
}