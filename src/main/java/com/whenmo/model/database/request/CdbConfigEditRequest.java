package com.whenmo.model.database.request;

import com.whenmo.model.database.domain.CdbConfig;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CdbConfigEditRequest {

    @NotBlank(message = "顯示名稱不可為空")
    private String name;

    @NotBlank(message = "cdb 檔案路徑不能為空")
    private String filePath;

    @Builder.Default
    private String picCdn = null;

    public CdbConfig toCdbConfig(String trimName) {
        return CdbConfig.builder().name(trimName).filePath(filePath).picCdn(picCdn).build();
    }

    public void updateCdbConfig(CdbConfig cdbConfig, String trimName) {
        if (cdbConfig == null)
            return;
        cdbConfig.setName(trimName);
        cdbConfig.setFilePath(filePath);
        cdbConfig.setPicCdn(picCdn);
    }
}