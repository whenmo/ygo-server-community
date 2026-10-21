package com.whenmo.model.cardinfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardInfoItem {
    private String key;
    private long code;
    private String name;
}