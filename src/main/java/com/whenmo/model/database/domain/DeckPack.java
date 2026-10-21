package com.whenmo.model.database.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "deck_packs")
@NoArgsConstructor
@AllArgsConstructor
public class DeckPack {

    @Id
    @Column(name = "name", nullable = false, unique = true)
    private String name;
}