package com.whenmo.model.database.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiyerString implements Serializable {

    public static final String TYPE_SETNAME = "setname";
    public static final String TYPE_COUNTER = "counter";
    public static final String TYPE_VICTORY = "victory";
    public static final String TYPE_INTERNAL = "internal";

    @Column(name = "cdb_id", nullable = false)
    private Integer cdbId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;
}