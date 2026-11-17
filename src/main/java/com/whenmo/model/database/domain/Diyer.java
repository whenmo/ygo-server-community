package com.whenmo.model.database.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Formula;

@Entity
@Table(name = "diyers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Diyer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "cdb_id", updatable = false)
    private Integer cdbId;

    @Formula("(SELECT c.name FROM cdb_configs c WHERE c.id = cdb_id)")
    private String cdbName;

    @Column(name = "user_id")
    private Integer userId;

    @Formula("(SELECT u.name FROM users u WHERE u.id = user_id)")
    private String userName;

    // 卡號區間 (diyer_code_ranges 1 ~ 2000)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "diyer_code_ranges", joinColumns = @JoinColumn(name = "diyer_id", referencedColumnName = "id"))
    @Column(name = "code_range")
    @Builder.Default
    private List<Integer> codeRanges = new ArrayList<>();

    // 字段區間 (diyer_set_ranges, 1 ~ 255，即 0x01 ~ 0xFF)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "diyer_set_ranges", joinColumns = @JoinColumn(name = "diyer_id", referencedColumnName = "id"))
    @Column(name = "set_range")
    @Builder.Default
    private List<Integer> setRanges = new ArrayList<>();

    // 字段、指示物與特殊勝利共用 diyer_strings
    @OneToMany(mappedBy = "diyer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DiyerString> strings = new ArrayList<>();
}