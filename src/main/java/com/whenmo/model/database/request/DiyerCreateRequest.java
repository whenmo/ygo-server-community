package com.whenmo.model.database.request;

import com.whenmo.model.database.domain.Diyer;
import com.whenmo.model.database.domain.DiyerString;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiyerCreateRequest {

    @NotBlank(message = "作者名稱不可為空")
    private String name;
    private Integer userId;
    private Set<Integer> codeRanges;
    private Set<Integer> setRanges;
    @Valid
    private Set<DiyerStringRequest> strings;

    public Diyer toDiyerWithoutUser() {
        Set<DiyerString> fullStrings = (strings == null) ? new HashSet<>()
                : strings.stream().map(DiyerStringRequest::toDiyerString).collect(Collectors.toSet());

        return Diyer.builder().name(name)
                .codeRanges(codeRanges != null ? codeRanges : new HashSet<>())
                .setRanges(setRanges != null ? setRanges : new HashSet<>())
                .strings(fullStrings).build();
    }
}