package com.whenmo.model.database.request;

import com.whenmo.model.database.domain.Diyer;
import com.whenmo.model.database.domain.DiyerString;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiyerStringEditRequest {
    @NotBlank(message = "類型不可為空")
    @Pattern(regexp = "^(setname|counter|victory|hint)$", message = "無效的 type 類型")
    private String type;

    @Min(value = 1, message = "code must >= 1")
    @Max(value = 65535, message = "code must <= 0xFFFF")
    @Builder.Default
    private Integer code = null;

    @Builder.Default
    private String name = "???";

    public DiyerString toDiyerString(String trimName, Diyer diyer) {
        return DiyerString.builder()
                .type(type)
                .code(code)
                .name(trimName)
                .diyer(diyer)
                .build();
    }

    public void updateDiyerString(DiyerString diyerString, String trimName) {
        if (diyerString == null)
            return;
        diyerString.setType(type);
        diyerString.setCode(code);
        diyerString.setName(trimName);
    }
}