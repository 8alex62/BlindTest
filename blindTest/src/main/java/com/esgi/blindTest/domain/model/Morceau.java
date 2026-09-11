package com.esgi.blindTest.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Porteur de donnees. Identifie par son nom, qui est unique au catalogue.
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Morceau {

    @NonNull
    private String nom;

    // Adresse du fichier audio joue par le navigateur.
    private String urlAudio;
}
