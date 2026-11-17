package com.whenmo.model.database.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "diyer_strings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DiyerString {

    public static final String TYPE_SETNAME = "setname";
    public static final String TYPE_COUNTER = "counter";
    public static final String TYPE_VICTORY = "victory";
    public static final String TYPE_HINT = "hint";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "code")
    private Integer code;

    @Column(name = "name", nullable = false)
    private String name;

    // 讀取用
    @Column(name = "diyer_id", insertable = false, updatable = false)
    private Integer diyerId;

    // 寫入與關聯用
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diyer_id", nullable = false)
    private Diyer diyer;
}