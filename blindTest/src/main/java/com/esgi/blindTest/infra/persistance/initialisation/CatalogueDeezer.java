package com.esgi.blindTest.infra.persistance.initialisation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Recherche un morceau sur l'API publique de Deezer, qui expose pour chaque piste
 * un extrait de trente secondes au format MP3, lisible directement par la balise audio.
 *
 * Le service est facultatif : toute panne se traduit par un Optional vide, et
 * AjoutMorceaux retombe alors sur son morceau de secours.
 */
@Component
public class CatalogueDeezer {

    private static final Logger JOURNAL = LoggerFactory.getLogger(CatalogueDeezer.class);
    private static final String RECHERCHE = "https://api.deezer.com/search?limit=1&q={requete}";
    private static final Duration DELAI = Duration.ofSeconds(3);

    private final RestClient restClient;
    private final boolean actif;

    public CatalogueDeezer(@Value("${app.catalogue.deezer.actif:true}") boolean actif) {
        this.actif = actif;
        // Delais courts : le demarrage de l'application ne doit pas attendre Deezer.
        SimpleClientHttpRequestFactory fabrique = new SimpleClientHttpRequestFactory();
        fabrique.setConnectTimeout(DELAI);
        fabrique.setReadTimeout(DELAI);
        this.restClient = RestClient.builder().requestFactory(fabrique).build();
    }

    public Optional<MorceauTrouve> chercher(String requete) {
        if (!actif) {
            return Optional.empty();
        }
        try {
            ReponseDeezer reponse = restClient.get()
                    .uri(RECHERCHE, requete)
                    .retrieve()
                    .body(ReponseDeezer.class);

            if (reponse == null || reponse.data() == null || reponse.data().isEmpty()) {
                JOURNAL.warn("Aucun resultat Deezer pour \"{}\"", requete);
                return Optional.empty();
            }
            Piste piste = reponse.data().getFirst();
            if (piste.titre() == null || piste.extrait() == null || piste.extrait().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(new MorceauTrouve(piste.titre(), piste.extrait()));

        } catch (RuntimeException erreur) {
            JOURNAL.warn("Deezer injoignable pour \"{}\", repli sur le morceau de secours ({})",
                    requete, erreur.getMessage());
            return Optional.empty();
        }
    }

    public record MorceauTrouve(String titre, String urlAudio) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ReponseDeezer(List<Piste> data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Piste(@JsonProperty("title_short") String titre,
                         @JsonProperty("preview") String extrait) {
    }
}
