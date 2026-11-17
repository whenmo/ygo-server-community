package com.whenmo.model.cdb.response;

import java.util.HashSet;
import java.util.Set;

import com.whenmo.model.cardinfo.CardType;
import com.whenmo.model.cdb.domain.Card;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {

    private Integer cdbId;
    private String picCdn;
    private Integer id;
    private Integer rule;
    private Integer alias;
    private Set<Integer> setcode;
    private Integer type;
    private Integer atk;
    private Integer def;
    private Integer level;
    private Integer scale;
    private Integer linkMarker;
    private Integer race;
    private Integer attribute;
    private Long category;
    private String name;
    private String desc;

    public static CardResponse fromCard(Card card, Integer cdbId, String picCdn) {
        if (card == null)
            return null;

        Integer typ = parseInteger(card.getType());

        Integer def = null;
        Integer linkMarker = null;
        if (CardType.isType(typ, CardType.LINK)) {
            linkMarker = parseInteger(card.getDef());
        } else {
            def = parseInteger(card.getDef());
        }

        Integer rawLevel = parseInteger(card.getLevel());
        Integer level = rawLevel & 0xFFFF;
        Integer scale = null;
        if (CardType.isType(typ, CardType.PENDULUM)) {
            scale = (rawLevel >> 24) & 0xFF;
        }

        Long category = card.getCategory();
        if (category == null)
            category = 0L;

        Integer cid = parseInteger(card.getId());
        String pic = picCdn == null ? null : (picCdn + "/" + cid + ".jpg");
        return CardResponse.builder()
                .cdbId(cdbId)
                .picCdn(pic)
                .id(cid)
                .rule(parseInteger(card.getOt()))
                .alias(parseInteger(card.getAlias()))
                .setcode(parseSetCode(card.getSetcode()))
                .type(typ)
                .atk(parseInteger(card.getAtk()))
                .def(def)
                .level(level)
                .scale(scale)
                .linkMarker(linkMarker)
                .race(parseInteger(card.getRace()))
                .attribute(parseInteger(card.getAttribute()))
                .category(category)
                .name(card.getName())
                .desc(card.getDesc())
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
        if (setcode == null || setcode == 0L) {
            return setcodes;
        }

        long temp = setcode;
        while (temp > 0) {
            int code = (int) (temp & 0xFFFF);
            if (code != 0)
                setcodes.add(code);
            temp >>= 16;
        }
        return setcodes;
    }
}
