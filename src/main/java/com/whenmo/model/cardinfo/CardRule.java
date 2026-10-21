package com.whenmo.model.cardinfo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CardRule {
    OCG(0x1, "OCG"),
    TCG(0x2, "TCG"),
    OTCG(0x3, "O&T"),
    CUSTOM(0x4, "自定義"),
    CCG(0x8, "簡中");

    private final int code;
    private final String name;
}