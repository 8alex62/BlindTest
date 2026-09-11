package com.esgi.blindTest.infra.persistance.initialisation;

import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.EtatLecture;
import com.esgi.blindTest.domain.model.StatutBlindTest;
import com.esgi.blindTest.infra.persistance.entity.BlindTestEntity;
import com.esgi.blindTest.infra.persistance.entity.MorceauEntity;
import com.esgi.blindTest.infra.persistance.repository.BlindTestJpaRepository;
import com.esgi.blindTest.infra.persistance.repository.MorceauJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Le blind test de demonstration est cree en attente, avec ses sept morceaux.
 */
class AjoutBlindTestTest {

    @Mock
    BlindTestJpaRepository blindTestJpaRepository;

    @Mock
    MorceauJpaRepository morceauJpaRepository;

    @InjectMocks
    AjoutBlindTest ajoutBlindTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void cree_le_blind_test_avec_sept_morceaux_et_en_attente() {
        when(blindTestJpaRepository.findByNom(anyString())).thenReturn(Optional.empty());
        when(morceauJpaRepository.findAll()).thenReturn(catalogue(BlindTest.NOMBRE_DE_MORCEAUX));

        ajoutBlindTest.init();

        ArgumentCaptor<BlindTestEntity> capteur = ArgumentCaptor.forClass(BlindTestEntity.class);
        verify(blindTestJpaRepository).save(capteur.capture());

        BlindTestEntity cree = capteur.getValue();
        assertEquals("Blind Test n°1", cree.getNom());
        assertEquals(BlindTest.NOMBRE_DE_MORCEAUX, cree.getMorceaux().size());
        assertEquals(StatutBlindTest.EN_ATTENTE, cree.getStatut());
        assertEquals(EtatLecture.PAUSE, cree.getEtatLecture());
        assertTrue(cree.getParticipations().isEmpty());
    }

    @Test
    void ne_recree_rien_si_le_blind_test_existe_deja() {
        when(blindTestJpaRepository.findByNom(anyString()))
                .thenReturn(Optional.of(new BlindTestEntity()));

        ajoutBlindTest.init();

        verify(morceauJpaRepository, never()).findAll();
        verify(blindTestJpaRepository, never()).save(any(BlindTestEntity.class));
    }

    @Test
    void ne_cree_rien_si_le_catalogue_est_incomplet() {
        when(blindTestJpaRepository.findByNom(anyString())).thenReturn(Optional.empty());
        when(morceauJpaRepository.findAll()).thenReturn(catalogue(5));

        ajoutBlindTest.init();

        verify(blindTestJpaRepository, never()).save(any(BlindTestEntity.class));
    }

    private static List<MorceauEntity> catalogue(int nombre) {
        return IntStream.rangeClosed(1, nombre)
                .mapToObj(numero -> new MorceauEntity("Morceau " + numero,
                        "https://exemple.test/" + numero + ".mp3"))
                .toList();
    }
}
