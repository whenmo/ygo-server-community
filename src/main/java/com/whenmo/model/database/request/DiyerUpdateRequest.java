package com.whenmo.model.database.request;

import lombok.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.stream.Collectors;
import java.util.Set;

import com.whenmo.model.database.domain.Diyer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiyerUpdateRequest {

    private Integer id;
    @NotBlank(message = "作者名稱不可為空")
    private String name;
    private Integer userId;
    private Set<Integer> codeRanges;
    private Set<Integer> setRanges;
    @Valid
    private Set<DiyerStringRequest> strings;

    public void updateDiyerWithoutUser(Diyer model) {
        if (model == null)
            return;

        if (this.name != null)
            model.setName(this.name);
        if (this.codeRanges != null)
            model.setCodeRanges(this.codeRanges);
        if (this.setRanges != null)
            model.setSetRanges(this.setRanges);
        if (this.strings != null)
            model.setStrings(this.strings.stream().map(DiyerStringRequest::toDiyerString).collect(Collectors.toSet()));
    }
}