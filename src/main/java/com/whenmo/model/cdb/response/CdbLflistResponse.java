package com.whenmo.model.cdb.response;

import java.util.Set;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CdbLflistResponse {

    private int cdbId;
    private String version;
    private Set<Integer> forbiddenIds;
    private Set<Integer> limitIds;
    private Set<Integer> semiLimitIds;

}
