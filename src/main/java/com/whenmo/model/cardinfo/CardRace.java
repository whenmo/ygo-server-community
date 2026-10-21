package com.whenmo.model.cardinfo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CardRace {
    WARRIOR(0x1, "战士"),
    SPELLCASTER(0x2, "魔法师"),
    FAIRY(0x4, "天使"),
    FIEND(0x8, "恶魔"),
    ZOMBIE(0x10, "不死"),
    MACHINE(0x20, "机械"),
    AQUA(0x40, "水"),
    PYRO(0x80, "炎"),
    ROCK(0x100, "岩石"),
    WINGED_BEAST(0x200, "鸟兽"),
    PLANT(0x400, "植物"),
    INSECT(0x800, "昆虫"),
    THUNDER(0x1000, "雷"),
    DRAGON(0x2000, "龙"),
    BEAST(0x4000, "兽"),
    BEAST_WARRIOR(0x8000, "兽战士"),
    DINOSAUR(0x10000, "恐龙"),
    FISH(0x20000, "鱼"),
    SEA_SERPENT(0x40000, "海龙"),
    REPTILE(0x80000, "爬虫类"),
    PSYCHIC(0x100000, "念动力"),
    DIVINE_BEAST(0x200000, "幻神兽"),
    CREATOR_GOD(0x400000, "创造神"),
    WYRM(0x800000, "幻龙"),
    CYBERSE(0x1000000, "电子界"),
    ILLUSION(0x2000000, "幻想魔");

    private final int code;
    private final String name;

    // 判斷函數

    public static boolean containRace(int cardRace, CardRace targetRace) {
        return (cardRace & targetRace.getCode()) != 0;
    }

    public static boolean isRace(int cardRace, CardRace targetRace) {
        return (cardRace & targetRace.getCode()) == targetRace.getCode();
    }

    public static boolean equalRace(int cardRace, CardRace targetRace) {
        return cardRace == targetRace.getCode();
    }
}