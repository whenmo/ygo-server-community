package com.whenmo.model.cdb.domain;

import lombok.Data;

@Data
public class Card {
    private Integer id;
    private Integer ot;
    private Integer alias;
    private Long setcode;
    private Integer type;
    private Integer atk;
    private Integer def;
    private Integer level;
    private Integer race;
    private Integer attribute;
    private Long category;
    private String name;
    private String desc;
}
