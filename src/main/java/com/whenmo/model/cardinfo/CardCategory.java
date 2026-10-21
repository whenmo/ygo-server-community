package com.whenmo.model.cardinfo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CardCategory {
    SPELL_TRAP_DESTROY(0x1L, "魔陷破坏"),
    MONSTER_DESTROY(0x2L, "怪兽破坏"),
    BANISH(0x4L, "卡片除外"),
    TO_GRAVEYARD(0x8L, "送去墓地"),
    TO_HAND(0x10L, "返回手卡"),
    TO_DECK(0x20L, "返回卡组"),
    HAND_DESTRUCTION(0x40L, "手卡破坏"),
    DECK_DESTRUCTION(0x80L, "卡组破坏"),
    DRAW(0x100L, "抽卡辅助"),
    SEARCH(0x200L, "卡组检索"),
    RECYCLE(0x400L, "卡片回收"),
    POSITION_CHANGE(0x800L, "表示形式"),
    CONTROL(0x1000L, "控制权"),
    ATK_DEF_CHANGE(0x2000L, "攻守变化"),
    PIERCE(0x4000L, "穿刺伤害"),
    MULTI_ATTACK(0x8000L, "多次攻击"),
    ATTACK_LIMIT(0x10000L, "攻击限制"),
    DIRECT_ATTACK(0x20000L, "直接攻击"),
    SPECIAL_SUMMON(0x40000L, "特殊召唤"),
    TOKEN(0x80000L, "衍生物"),
    RACE_RELATED(0x100000L, "种族相关"),
    ATTRIBUTE_RELATED(0x200000L, "属性相关"),
    LP_DAMAGE(0x400000L, "LP伤害"),
    LP_RECOVERY(0x800000L, "LP回复"),
    DESTROY_RESISTANCE(0x1000000L, "破坏耐性"),
    EFFECT_RESISTANCE(0x2000000L, "效果耐性"),
    COUNTER(0x4000000L, "指示物"),
    GAMBLE(0x8000000L, "赌博相关"),
    FUSION_RELATED(0x10000000L, "融合相关"),
    SYNCHRO_RELATED(0x20000000L, "同调相关"),
    XYZ_RELATED(0x40000000L, "超量相关"),
    DISABLE_EFFECT(0x80000000L, "效果无效");

    private final long code;
    private final String name;

    // 判斷函數

    public static boolean containCategory(long cardCategory, CardCategory targetCategory) {
        return (cardCategory & targetCategory.getCode()) != 0;
    }

    public static boolean isCategory(long cardCategory, CardCategory targetCategory) {
        return (cardCategory & targetCategory.getCode()) == targetCategory.getCode();
    }

    public static boolean equalCategory(long cardCategory, CardCategory targetCategory) {
        return cardCategory == targetCategory.getCode();
    }
}