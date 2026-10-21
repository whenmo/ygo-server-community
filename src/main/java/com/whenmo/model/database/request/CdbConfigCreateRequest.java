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
public class CdbConfigCreateRequest {

    @NotBlank(message = "資料庫顯示名稱不能為空")
    private String name;

    @NotBlank(message = "cdb 檔案路徑不能為空")
    private String filePath;

    public CdbConfig toModel() {
        return CdbConfig.builder().name(this.name).filePath(this.filePath).build();
    }
}