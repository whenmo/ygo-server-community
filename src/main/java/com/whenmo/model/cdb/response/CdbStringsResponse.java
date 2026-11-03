package com.whenmo.model.cdb.response;

import java.util.List;

import com.whenmo.model.cdb.domain.CdbStringsItem;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CdbStringsResponse {

    private int cdbId;
    private List<CdbStringsItem> setnames;
    private List<CdbStringsItem> counters;
    private List<CdbStringsItem> victorys;
}
