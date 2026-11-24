package com.whenmo.model.database.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "cdb_configs")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CdbConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "file_path", nullable = false, unique = true)
    private String filePath;

    @Column(name = "pic_cdn", nullable = true)
    private String picCdn;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "cdb_pic_cdns", joinColumns = @JoinColumn(name = "cdb_id"))
    @Column(name = "pic_cdn", nullable = false)
    private List<String> backupPicCdns = new ArrayList<>();
}