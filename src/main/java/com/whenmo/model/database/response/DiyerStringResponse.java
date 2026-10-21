package com.whenmo.model.database.response;

import com.whenmo.model.database.domain.DiyerString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiyerStringResponse {
    private Integer cdbId;
    private String type;
    private String code;
    private String name;

    public static DiyerStringResponse fromModel(DiyerString diyerString) {
        if (diyerString == null)
            return null;

        return DiyerStringResponse.builder().cdbId(diyerString.getCdbId()).type(diyerString.getType())
                .code(diyerString.getCode()).name(diyerString.getName()).build();
    }
}