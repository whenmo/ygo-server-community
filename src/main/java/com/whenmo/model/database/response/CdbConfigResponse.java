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

    public static CdbConfigResponse fromModel(CdbConfig model) {
        return CdbConfigResponse.builder()
                .id(model.getId())
                .name(model.getName())
                .filePath(model.getFilePath())
                .build();
    }
}