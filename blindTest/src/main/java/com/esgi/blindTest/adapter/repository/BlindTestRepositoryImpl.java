package com.esgi.blindTest.adapter.repository;

import com.esgi.blindTest.adapter.mapper.BlindTestEntityMapper;
import com.esgi.blindTest.domain.exception.BlindTestIntrouvableException;
import com.esgi.blindTest.domain.exception.ConflitDeConcurrenceException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.StatutBlindTest;
import com.esgi.blindTest.domain.repository.BlindTestRepository;
import com.esgi.blindTest.infra.persistance.entity.BlindTestEntity;
import com.esgi.blindTest.infra.persistance.repository.BlindTestJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class BlindTestRepositoryImpl implements BlindTestRepository {

    private final BlindTestJpaRepository blindTestJpaRepository;
    private final BlindTestEntityMapper blindTestEntityMapper;

    @Override
    @Transactional
    public BlindTest save(BlindTest blindTest) {
        if (blindTest.getId() == null) {
            return blindTestEntityMapper.toEntity(
                    blindTestJpaRepository.save(blindTestEntityMapper.toDto(blindTest)));
        }
        BlindTestEntity entity = blindTestJpaRepository.findById(blindTest.getId())
                .orElseThrow(BlindTestIntrouvableException::new);
        // Verrou optimiste sur agregat detache : le domaine transporte la version lue.
        if (blindTest.getVersion() != null && !blindTest.getVersion().equals(entity.getVersion())) {
            throw new ConflitDeConcurrenceException();
        }
        blindTestEntityMapper.mettreAJour(blindTest, entity);
        return blindTestEntityMapper.toEntity(blindTestJpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BlindTest> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        // Le mapping se fait dans la transaction : les collections paresseuses sont lisibles.
        return blindTestJpaRepository.findById(id).map(blindTestEntityMapper::toEntity);
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
        int lignesModifiees = blindTestJpaRepository.reserverLaReponse(
                blindTest.getId(),
                blindTest.getIdParticipantReservataire(),
                blindTest.getIndexMorceauCourant());
        return lignesModifiees == 1;
    }
}
