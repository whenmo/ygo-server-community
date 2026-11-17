package com.whenmo.model.database.dto;

import com.whenmo.model.database.domain.DiyerString;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class DiyerStringDto {
    private Integer id;
    private String type;
    private Integer code;
    private String name;

    public static DiyerStringDto fromDiyerString(DiyerString diyerString) {
        if (diyerString == null)
            return null;

        return new DiyerStringDto(
                diyerString.getId(),
                diyerString.getType(),
                diyerString.getCode(),
                diyerString.getName());
    }
}
