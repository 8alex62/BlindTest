package com.esgi.blindTest.infra.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "morceau")
@Getter
@Setter
@NoArgsConstructor
public class MorceauEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String nom;

    // Les URL signees des fournisseurs depassent largement les 255 caracteres par defaut.
    @Column(name = "url_audio", length = 1000)
    private String urlAudio;

    public MorceauEntity(String nom, String urlAudio) {
        this.nom = nom;
        this.urlAudio = urlAudio;
    }
}
