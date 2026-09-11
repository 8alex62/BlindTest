package com.esgi.blindTest.infra.persistance.initialisation;

import com.esgi.blindTest.infra.persistance.entity.ParticipantEntity;
import com.esgi.blindTest.infra.persistance.repository.ParticipantJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Les deux comptes de demonstration sont crees une fois, et une seule.
 */
class AjoutParticipantsTest {

    @Mock
    ParticipantJpaRepository participantJpaRepository;

    @InjectMocks
    AjoutParticipants ajoutParticipants;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void cree_les_deux_comptes_de_demonstration() {
        when(participantJpaRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        ajoutParticipants.init();

        ArgumentCaptor<ParticipantEntity> capteur =
                ArgumentCaptor.forClass(ParticipantEntity.class);
        verify(participantJpaRepository, times(2)).save(capteur.capture());

        List<String> emails = capteur.getAllValues().stream().map(ParticipantEntity::getEmail).toList();
        assertEquals(List.of("user1@test.fr", "user2@test.fr"), emails);
    }

    @Test
    void le_mot_de_passe_respecte_la_regle_des_huit_caracteres() {
        when(participantJpaRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        ajoutParticipants.init();

        ArgumentCaptor<ParticipantEntity> capteur =
                ArgumentCaptor.forClass(ParticipantEntity.class);
        verify(participantJpaRepository, times(2)).save(capteur.capture());

        capteur.getAllValues().forEach(participant -> {
            assertEquals("12345678", participant.getMotDePasse());
            assertTrue(participant.getMotDePasse().length() >= 8);
        });
    }

    @Test
    void ne_recree_rien_si_les_comptes_existent_deja() {
        when(participantJpaRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(new ParticipantEntity("user1@test.fr", "12345678")));

        ajoutParticipants.init();

        verify(participantJpaRepository, never()).save(any(ParticipantEntity.class));
    }
}
