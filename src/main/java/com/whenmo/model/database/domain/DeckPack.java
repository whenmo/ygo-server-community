package com.whenmo.model.database.domain;

import org.hibernate.annotations.Formula;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Data
@Entity
@Table(name = "deck_packs")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DeckPack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "user_id", updatable = false)
    private Integer userId;

    @Formula("(SELECT u.name FROM users u WHERE u.id = user_id)")
    private String userName;

    public Boolean getIsPublic() {
        return userId == null;
    }
}