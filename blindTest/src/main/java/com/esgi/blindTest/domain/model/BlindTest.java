package com.esgi.blindTest.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Porteur de donnees. Identifie par son nom, qui est unique.
 * Les regles du jeu sont appliquees par les use cases du package domain.usecase.
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class BlindTest {

    public static final int NOMBRE_MAXIMUM_DE_PARTICIPANTS = 3;
    public static final int NOMBRE_DE_MORCEAUX = 7;

    @NonNull
    private String nom;

    private List<Participation> participations = new ArrayList<>();

    private List<Morceau> morceaux = new ArrayList<>();

    // Distingue en attente, en cours et termine.
    private StatutBlindTest statut = StatutBlindTest.EN_ATTENTE;

    // Position dans la sequence des sept morceaux.
    private int indexMorceauCourant;

    // Indique au navigateur de jouer ou de mettre en pause.
    private EtatLecture etatLecture = EtatLecture.PAUSE;

    // Participant qui a gagne le premier clic sur le morceau courant.
    private Participant reservataire;

    // Jeton de verrou optimiste, transporte entre la base et le domaine.
    private Long version;
}
