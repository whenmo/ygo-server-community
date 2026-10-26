package com.whenmo.model.database.response;

import com.whenmo.model.database.domain.*;
import com.whenmo.model.database.dto.UserDto;

import lombok.*;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiyerResponse {

    private Integer id;
    private String name;
    private UserDto user;
    private Set<Integer> codeRanges;
    private Set<Integer> setRanges;
    private Set<DiyerStringResponse> strings;

    public static DiyerResponse fromModel(Diyer model) {
        if (model == null)
            return null;

        Set<DiyerString> strings = model.getStrings() == null ? Set.of() : model.getStrings();

        return DiyerResponse.builder()
                .id(model.getId())
                .name(model.getName())
                .user(UserDto.fromModel(model.getUser()))
                .codeRanges(model.getCodeRanges())
                .setRanges(model.getSetRanges())
                .strings(strings.stream().map(DiyerStringResponse::fromModel).collect(Collectors.toSet()))
                .build();

    }
}