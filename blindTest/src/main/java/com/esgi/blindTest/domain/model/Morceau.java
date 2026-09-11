package com.esgi.blindTest.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Morceau {

    private static final Pattern DIACRITIQUES = Pattern.compile("\\p{M}+");
    private static final Pattern ESPACES = Pattern.compile("\\s+");

    // Champ technique : identité persistante du morceau dans le catalogue.
    private Long id;

    @NonNull
    private String nom;

    // Champ technique : adresse du fichier audio joué par le navigateur.
    private String urlAudio;

    /**
     * Règle métier : la comparaison du titre ignore la casse, les accents
     * et les espaces superflus.
     */
    public boolean correspondA(String proposition) {
        return normaliser(proposition).equals(normaliser(nom));
    }

    static String normaliser(String valeur) {
        if (valeur == null) {
            return "";
        }
        String sansLigature = valeur
                .replace('’', '\'')
                .replace("Œ", "OE").replace("œ", "oe")
                .replace("Æ", "AE").replace("æ", "ae");
        String sansAccent = DIACRITIQUES
                .matcher(Normalizer.normalize(sansLigature, Normalizer.Form.NFD))
                .replaceAll("");
        return ESPACES.matcher(sansAccent.trim()).replaceAll(" ").toLowerCase(Locale.ROOT);
    }
}
