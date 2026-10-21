package com.whenmo.model.cdb;

import lombok.Data;

@Data
public class CdbCard {
    private int id;
    private Integer ot;
    private Integer alias;
    private Long setcode;
    private Long type;
    private Integer atk;
    private Integer def;
    private Integer level;
    private Integer race;
    private Integer attribute;
    private Long category;
    private String name;
    private String desc;

    public static boolean isValidId(Integer value) {
        return value != null && value > 0 && value < 1_000_000_000;
    }
}