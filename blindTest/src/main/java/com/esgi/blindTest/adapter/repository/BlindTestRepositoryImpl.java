package com.esgi.blindTest.adapter.repository;

import com.esgi.blindTest.adapter.mapper.BlindTestEntityMapper;
import com.esgi.blindTest.domain.exception.ConflitDeConcurrenceException;
import com.esgi.blindTest.domain.exception.MorceauIntrouvableException;
import com.esgi.blindTest.domain.exception.ParticipantIntrouvableException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.StatutBlindTest;
import com.esgi.blindTest.domain.repository.BlindTestRepository;
import com.esgi.blindTest.infra.persistance.entity.BlindTestEntity;
import com.esgi.blindTest.infra.persistance.entity.MorceauEntity;
import com.esgi.blindTest.infra.persistance.entity.ParticipantEntity;
import com.esgi.blindTest.infra.persistance.repository.BlindTestJpaRepository;
import com.esgi.blindTest.infra.persistance.repository.MorceauJpaRepository;
import com.esgi.blindTest.infra.persistance.repository.ParticipantJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class BlindTestRepositoryImpl implements BlindTestRepository {

    private final BlindTestJpaRepository blindTestJpaRepository;
    private final MorceauJpaRepository morceauJpaRepository;
    private final ParticipantJpaRepository participantJpaRepository;
    private final BlindTestEntityMapper blindTestEntityMapper;

    @Override
    @Transactional
    public BlindTest save(BlindTest blindTest) {
        Optional<BlindTestEntity> existante = blindTestJpaRepository.findByNom(blindTest.getNom());

        if (existante.isEmpty()) {
            BlindTestEntity nouvelle = blindTestEntityMapper.toDto(blindTest);
            nouvelle.setMorceaux(morceauxGeres(blindTest));
            nouvelle.setReservataire(reservataireGere(blindTest));
            return blindTestEntityMapper.toEntity(blindTestJpaRepository.save(nouvelle));
        }

        BlindTestEntity entity = existante.get();
        // Verrou optimiste sur agregat detache : le domaine transporte la version lue.
        if (blindTest.getVersion() != null && !blindTest.getVersion().equals(entity.getVersion())) {
            throw new ConflitDeConcurrenceException();
        }
        blindTestEntityMapper.mettreAJour(blindTest, entity);
        entity.setReservataire(reservataireGere(blindTest));
        return blindTestEntityMapper.toEntity(blindTestJpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BlindTest> findByNom(String nom) {
        if (nom == null) {
            return Optional.empty();
        }
        // Le mapping se fait dans la transaction : les collections paresseuses sont lisibles.
        return blindTestJpaRepository.findByNom(nom).map(blindTestEntityMapper::toEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlindTest> findEnCours() {
        return blindTestJpaRepository
                .findByStatutIn(List.of(StatutBlindTest.EN_ATTENTE, StatutBlindTest.EN_COURS))
                .stream()
                .map(blindTestEntityMapper::toEntity)
                .toList();
    }

    @Override
    @Transactional
    public boolean reserverLaReponse(BlindTest blindTest) {
        ParticipantEntity reservataire = reservataireGere(blindTest);
        if (reservataire == null) {
            return false;
        }
        int lignesModifiees = blindTestJpaRepository.reserverLaReponse(
                blindTest.getNom(),
                reservataire.getId(),
                blindTest.getIndexMorceauCourant());
        return lignesModifiees == 1;
    }

    /**
     * Les morceaux existent deja au catalogue : on rattache les entites gerees,
     * retrouvees par leur nom.
     */
    private List<MorceauEntity> morceauxGeres(BlindTest blindTest) {
        List<MorceauEntity> morceaux = new ArrayList<>();
        blindTest.getMorceaux().forEach(morceau -> morceaux.add(
                morceauJpaRepository.findByNom(morceau.getNom())
                        .orElseThrow(MorceauIntrouvableException::new)));
        return morceaux;
    }

    private ParticipantEntity reservataireGere(BlindTest blindTest) {
        if (blindTest.getReservataire() == null) {
            return null;
        }
        return participantJpaRepository.findByEmail(blindTest.getReservataire().getEmail())
                .orElseThrow(ParticipantIntrouvableException::new);
    }
}
