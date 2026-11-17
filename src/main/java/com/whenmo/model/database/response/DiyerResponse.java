package com.whenmo.model.database.response;

import java.util.List;
import com.whenmo.model.database.domain.*;
import com.whenmo.model.database.dto.CdbInfoDto;
import com.whenmo.model.database.dto.DiyerStringDto;
import com.whenmo.model.database.dto.UserDto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiyerResponse {

    private Integer id;
    private String name;
    private UserDto user;
    private CdbInfoDto cdb;
    private List<Integer> codeRanges;
    private List<Integer> setRanges;
    private List<DiyerStringDto> strings;

    public static DiyerResponse fromDiyer(Diyer diyer) {
        if (diyer == null)
            return null;

        return DiyerResponse.builder()
                .id(diyer.getId()).name(diyer.getName())
                .user(UserDto.fromDiyer(diyer))
                .cdb(CdbInfoDto.fromDiyer(diyer))
                .codeRanges(diyer.getCodeRanges()).setRanges(diyer.getSetRanges())
                .strings(diyer.getStrings().stream().map(DiyerStringDto::fromDiyerString).toList())
                .build();
    }
}