package com.esgi.blindTest.infra.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entite d'association porteuse du score. La contrainte d'unicite double en base
 * la regle applicative "un participant ne rejoint qu'une fois le meme blind test".
 */
@Entity
@Table(name = "participation",
        uniqueConstraints = @UniqueConstraint(columnNames = {"blind_test_id", "participant_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ParticipationEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    private ParticipantEntity participant;

    // Reference inverse propre a la persistance : elle n'existe pas dans le domaine.
    @ManyToOne(optional = false)
    @JoinColumn(name = "blind_test_id", nullable = false)
    private BlindTestEntity blindTest;

    @Column(nullable = false)
    private int score;
}
