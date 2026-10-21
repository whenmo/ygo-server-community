package com.whenmo.model.database.request;

import com.whenmo.model.database.domain.DiyerString;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiyerStringRequest {
    @NotNull(message = "String 資料必須指定 CDB ID")
    private Integer cdbId;
    @NotBlank(message = "String 類型不可為空")
    @Pattern(regexp = "^(setname|counter|victory|internal)$", message = "無效的 type 類型")
    private String type;
    @NotBlank(message = "code 不可為空")
    private String code;
    @NotBlank(message = "name 不可為空")
    private String name;

    public DiyerString toDiyerString() {
        return DiyerString.builder().cdbId(cdbId).type(type).code(code).name(name).build();
    }

    public static DiyerStringRequest fromDiyerString(DiyerString diyerString) {
        return DiyerStringRequest.builder().cdbId(diyerString.getCdbId()).type(diyerString.getType())
                .code(diyerString.getCode()).name(diyerString.getName()).build();
    }
}