package com.whenmo.model.cdb;

import java.util.HashSet;
import java.util.Set;

import com.whenmo.model.cardinfo.CardType;
import com.whenmo.model.database.response.CdbConfigResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CdbCardResponse {

    private int cdbId;
    private int id;
    private int rule;
    private int alias;
    private Set<Integer> setcode;
    private long type;
    private int atk;
    private int def;
    private int level;
    private int scalePlus;
    private int linkMarker;
    private int race;
    private int attribute;
    private long category;
    private String name;
    private String desc;

    public static CdbCardResponse fromModel(CdbCard model, CdbConfigResponse configResponse) {
        CdbCardResponse response = fromModel(model);

        if (configResponse != null)
            response.setCdbId(configResponse.getId().intValue());

        return response;
    }

    private static CdbCardResponse fromModel(CdbCard model) {
        if (model == null) {
            return null;
        }

        long typ = model.getType();

        int def = 0;
        int linkMarker = 0;
        int rawDef = parseInteger(model.getDef());
        if (CardType.isType(typ, CardType.LINK)) {
            linkMarker = rawDef;
        } else {
            def = rawDef;
        }

        int rawLevel = parseInteger(model.getLevel());
        int level = rawLevel & 0xFFFF;
        int scalePlus = 0;
        if (CardType.isType(typ, CardType.PENDULUM)) {
            scalePlus = ((rawLevel >> 24) & 0xFF) + 1;
        }

        long category = 0;
        Long rawCategory = model.getCategory();
        if (rawCategory != null) {
            category = rawCategory;
        }

        return CdbCardResponse.builder()
                .id(parseInteger(model.getId()))
                .rule(parseInteger(model.getOt()))
                .alias(parseInteger(model.getAlias()))
                .setcode(parseSetCode(model.getSetcode()))
                .type(typ)
                .atk(parseInteger(model.getAtk()))
                .def(def)
                .level(level)
                .scalePlus(scalePlus)
                .linkMarker(linkMarker)
                .race(parseInteger(model.getRace()))
                .attribute(parseInteger(model.getAttribute()))
                .category(category)
                .name(model.getName())
                .desc(model.getDesc())
                .build();
    }

    private static int parseInteger(Integer integer) {
        return integer == null ? 0 : integer;
    }

    /**
     * 解析 64-bit setcode 為 Set<Integer>
     */
    private static Set<Integer> parseSetCode(Long setcode) {
        Set<Integer> setcodes = new HashSet<>();
        if (setcode == null || setcode == 0) {
            return setcodes;
        }

        long temp = setcode;
        while (temp > 0) {
            int code = (int) (temp & 0xFFFF);
            if (code != 0) {
                setcodes.add(code);
            }
            temp >>= 16;
        }
        return setcodes;
    }
}