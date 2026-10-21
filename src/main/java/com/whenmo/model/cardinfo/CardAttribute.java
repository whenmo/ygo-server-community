package com.whenmo.model.cardinfo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CardAttribute {
    EARTH(0x1, "地"),
    WATER(0x2, "水"),
    FIRE(0x4, "炎"),
    WIND(0x8, "风"),
    LIGHT(0x10, "光"),
    DARK(0x20, "暗"),
    DIVINE(0x40, "神");

    private final int code;
    private final String name;

    // 判斷函數

    public static boolean containAttribute(int cardAttribute, CardAttribute targetAttribute) {
        return (cardAttribute & targetAttribute.getCode()) != 0;
    }

    public static boolean isAttribute(int cardAttribute, CardAttribute targetAttribute) {
        return (cardAttribute & targetAttribute.getCode()) == targetAttribute.getCode();
    }

    public static boolean equalAttribute(int cardAttribute, CardAttribute targetAttribute) {
        return cardAttribute == targetAttribute.getCode();
    }
}