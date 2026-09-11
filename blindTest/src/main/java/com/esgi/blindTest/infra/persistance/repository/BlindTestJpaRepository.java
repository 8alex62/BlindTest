package com.esgi.blindTest.infra.persistance.repository;

import com.esgi.blindTest.domain.model.StatutBlindTest;
import com.esgi.blindTest.infra.persistance.entity.BlindTestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface BlindTestJpaRepository extends JpaRepository<BlindTestEntity, Long> {

    List<BlindTestEntity> findByStatutIn(Collection<StatutBlindTest> statuts);

    /**
     * Mise a jour conditionnelle : la ligne n'est modifiee que si personne n'a encore
     * reserve la reponse sur ce morceau. Le premier appel gagne, les suivants
     * recoivent zero ligne modifiee. C'est l'arbitrage atomique du premier clic.
     *
     * @return le nombre de lignes modifiees, 1 si cet appel a pris la main
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE blind_test"
            + "    SET id_participant_reservataire = :idParticipant,"
            + "        etat_lecture = 'PAUSE',"
            + "        version = version + 1"
            + "  WHERE id = :idBlindTest"
            + "    AND statut = 'EN_COURS'"
            + "    AND index_morceau_courant = :indexMorceau"
            + "    AND id_participant_reservataire IS NULL",
            nativeQuery = true)
    int reserverLaReponse(@Param("idBlindTest") Long idBlindTest,
                          @Param("idParticipant") Long idParticipant,
                          @Param("indexMorceau") int indexMorceau);
}
