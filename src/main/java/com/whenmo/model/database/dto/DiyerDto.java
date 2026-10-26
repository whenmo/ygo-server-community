package com.whenmo.model.database.dto;

import com.whenmo.model.database.domain.Diyer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DiyerDto {
    private Integer id;
    private String name;

    public static DiyerDto fromModel(Diyer diyer) {
        if (diyer == null)
            return null;
        return DiyerDto.builder().id(diyer.getId()).name(diyer.getName()).build();
    }
}
