package com.esgi.blindTest.infra.persistance.entity;

import com.esgi.blindTest.domain.model.EtatLecture;
import com.esgi.blindTest.domain.model.StatutBlindTest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blind_test")
@Getter
@Setter
@NoArgsConstructor
public class BlindTestEntity {

    // L'identifiant est une preoccupation de persistance : il ne sort pas de infra.
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String nom;

    // Lecture seule : les participations sont ecrites par ParticipationRepositoryImpl.
    @OneToMany(mappedBy = "blindTest")
    private List<ParticipationEntity> participations = new ArrayList<>();

    // Le catalogue de morceaux est partage : table de jointure ordonnee.
    @ManyToMany
    @JoinTable(name = "blind_test_morceau",
            joinColumns = @JoinColumn(name = "blind_test_id"),
            inverseJoinColumns = @JoinColumn(name = "morceau_id"))
    @OrderColumn(name = "position")
    private List<MorceauEntity> morceaux = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutBlindTest statut = StatutBlindTest.EN_ATTENTE;

    @Column(name = "index_morceau_courant", nullable = false)
    private int indexMorceauCourant;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat_lecture", nullable = false, length = 20)
    private EtatLecture etatLecture = EtatLecture.PAUSE;

    // Participant qui a gagne le premier clic sur le morceau courant.
    @ManyToOne
    @JoinColumn(name = "reservataire_id")
    private ParticipantEntity reservataire;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
