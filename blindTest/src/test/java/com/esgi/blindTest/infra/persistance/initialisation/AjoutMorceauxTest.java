package com.esgi.blindTest.infra.persistance.initialisation;

import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.infra.persistance.entity.MorceauEntity;
import com.esgi.blindTest.infra.persistance.initialisation.CatalogueDeezer.MorceauTrouve;
import com.esgi.blindTest.infra.persistance.repository.MorceauJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Le catalogue doit toujours compter sept morceaux, que Deezer reponde ou non.
 */
class AjoutMorceauxTest {

    @Mock
    MorceauJpaRepository morceauJpaRepository;

    @Mock
    CatalogueDeezer catalogueDeezer;

    @InjectMocks
    AjoutMorceaux ajoutMorceaux;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void insere_sept_morceaux_de_secours_quand_deezer_est_indisponible() {
        when(catalogueDeezer.chercher(anyString())).thenReturn(Optional.empty());
        when(morceauJpaRepository.findByNom(anyString())).thenReturn(Optional.empty());

        ajoutMorceaux.init();

        verify(morceauJpaRepository, times(BlindTest.NOMBRE_DE_MORCEAUX))
                .save(any(MorceauEntity.class));
    }

    @Test
    void utilise_le_titre_et_lextrait_renvoyes_par_deezer() {
        when(catalogueDeezer.chercher(anyString())).thenReturn(Optional.of(
                new MorceauTrouve("Bohemian Rhapsody", "https://exemple.test/extrait.mp3")));
        when(morceauJpaRepository.findByNom(anyString())).thenReturn(Optional.empty());

        ajoutMorceaux.init();

        ArgumentCaptor<MorceauEntity> capteur = ArgumentCaptor.forClass(MorceauEntity.class);
        verify(morceauJpaRepository, atLeastOnce()).save(capteur.capture());
        assertEquals("Bohemian Rhapsody", capteur.getValue().getNom());
        assertEquals("https://exemple.test/extrait.mp3", capteur.getValue().getUrlAudio());
    }

    @Test
    void nenregistre_rien_si_le_catalogue_est_deja_rempli() {
        when(catalogueDeezer.chercher(anyString())).thenReturn(Optional.empty());
        when(morceauJpaRepository.findByNom(anyString()))
                .thenReturn(Optional.of(new MorceauEntity("deja la", "https://exemple.test/x.mp3")));

        ajoutMorceaux.init();

        verify(morceauJpaRepository, never()).save(any(MorceauEntity.class));
    }
}
