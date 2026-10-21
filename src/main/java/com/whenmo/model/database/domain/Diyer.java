package com.whenmo.model.database.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

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
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    // ------------------------------------------------------------
    // 一對一關聯：Diyer 對應 User (外鍵 user_id 在 diyers 表)
    // ------------------------------------------------------------
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    private User user;

    // 卡號區間 (diyer_code_ranges 1 ~ 2000)
    @ElementCollection
    @CollectionTable(name = "diyer_code_ranges", joinColumns = @JoinColumn(name = "diyer_id", referencedColumnName = "id"))
    @Column(name = "code_range")
    @Builder.Default
    private Set<Integer> codeRanges = new HashSet<>();

    // 字段區間 (diyer_set_ranges, 1 ~ 255，即 0x01 ~ 0xFF)
    @ElementCollection
    @CollectionTable(name = "diyer_set_ranges", joinColumns = @JoinColumn(name = "diyer_id", referencedColumnName = "id"))
    @Column(name = "set_range")
    @Builder.Default
    private Set<Integer> setRanges = new HashSet<>();

    // 字段、指示物與特殊勝利共用 diyer_strings
    @ElementCollection
    @CollectionTable(name = "diyer_strings", joinColumns = @JoinColumn(name = "diyer_id", referencedColumnName = "id"))
    @Builder.Default
    private Set<DiyerString> strings = new HashSet<>();
}