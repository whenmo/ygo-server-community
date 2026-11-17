package com.whenmo.model.database.dto;

import com.whenmo.model.database.domain.Diyer;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class CdbInfoDto {
    private Integer id;
    private String name;

    public static CdbInfoDto fromDiyer(Diyer diyer) {
        if (diyer == null)
            return null;
        return new CdbInfoDto(diyer.getId(), diyer.getName());
    }
}
