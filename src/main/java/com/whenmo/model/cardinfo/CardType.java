package com.whenmo.model.cardinfo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CardType {
    MONSTER(0x1L, "怪獸"),
    SPELL(0x2L, "魔法"),
    TRAP(0x4L, "陷阱"),
    NORMAL(0x10L, "通常"),
    EFFECT(0x20L, "效果"),
    FUSION(0x40L, "融合"),
    RITUAL(0x80L, "儀式"),
    // TRAPMONSTER(0x100L, "陷阱怪兽"),
    SPIRIT(0x200L, "靈魂"),
    UNION(0x400L, "同盟"),
    DUAL(0x800L, "二重"),
    TUNER(0x1000L, "調整"),
    SYNCHRO(0x2000L, "同調"),
    TOKEN(0x4000L, "衍生"),
    QUICKPLAY(0x10000L, "速攻"),
    CONTINUOUS(0x20000L, "永續"),
    EQUIP(0x40000L, "裝備"),
    FIELD(0x80000L, "場地"),
    COUNTER(0x100000L, "反擊"),
    FLIP(0x200000L, "翻轉"),
    TOON(0x400000L, "卡通"),
    XYZ(0x800000L, "超量"),
    PENDULUM(0x1000000L, "靈擺"),
    SPSUMMON(0x2000000L, "特召"),
    LINK(0x4000000L, "連接");

    private final long code;
    private final String name;

    // 判斷函數
    public boolean isTypeMain() {
        return this == MONSTER || this == SPELL || this == TRAP;
    }

    public boolean isTypeMonster() {
        return this == NORMAL || this == EFFECT || this == FUSION || this == RITUAL || this == SPIRIT || this == UNION
                || this == DUAL || this == TUNER || this == SYNCHRO || this == TOKEN || this == FLIP
                || this == TOON || this == XYZ || this == PENDULUM || this == SPSUMMON || this == LINK;
    }

    public boolean isTypeSpellTrap() {
        return this == NORMAL || this == RITUAL || this == QUICKPLAY || this == CONTINUOUS || this == EQUIP
                || this == FIELD || this == COUNTER;
    }

    public static boolean containType(long cardType, CardType targetType) {
        return (cardType & targetType.getCode()) != 0;
    }

    public static boolean isType(long cardType, CardType targetType) {
        return (cardType & targetType.getCode()) == targetType.getCode();
    }

    public static boolean equalType(long cardType, CardType targetType) {
        return cardType == targetType.getCode();
    }
}