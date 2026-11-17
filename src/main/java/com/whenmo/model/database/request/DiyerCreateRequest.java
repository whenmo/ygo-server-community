package com.whenmo.model.database.request;

import com.whenmo.model.database.domain.Diyer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiyerCreateRequest {

    @NotBlank(message = "作者名稱不可為空")
    private String name;
    @NotNull(message = "數據庫不可為空")
    private Integer cdbId;
    @Builder.Default
    private Integer userId = null;
    @Builder.Default
    private List<@NotNull(message = "codeRanges must not null") @Min(value = 1, message = "codeRanges must >= 1") @Max(value = 2000, message = "codeRanges must <= 2000") Integer> codeRanges = new ArrayList<>();
    @Builder.Default
    private List<@NotNull(message = "setRanges must not null") @Min(value = 1, message = "setRanges must >= 1") @Max(value = 255, message = "setRanges must <= 0xFF") Integer> setRanges = new ArrayList<>();
    @Builder.Default
    private List<@Valid DiyerStringEditRequest> strings = new ArrayList<>();

    public Diyer toDiyer(String trimName) {
        return Diyer.builder().name(trimName).cdbId(cdbId).userId(userId).codeRanges(codeRanges).setRanges(setRanges)
                .build();
    }
}