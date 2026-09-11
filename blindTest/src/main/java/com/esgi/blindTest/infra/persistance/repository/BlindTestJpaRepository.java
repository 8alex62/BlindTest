package com.esgi.blindTest.infra.persistance.repository;

import com.esgi.blindTest.domain.model.StatutBlindTest;
import com.esgi.blindTest.infra.persistance.entity.BlindTestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BlindTestJpaRepository extends JpaRepository<BlindTestEntity, Long> {

    // Methode derivee
    Optional<BlindTestEntity> findByNom(String nom);

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
            + "    SET reservataire_id = :idParticipant,"
            + "        etat_lecture = 'PAUSE',"
            + "        version = version + 1"
            + "  WHERE nom = :nom"
            + "    AND statut = 'EN_COURS'"
            + "    AND index_morceau_courant = :indexMorceau"
            + "    AND reservataire_id IS NULL",
            nativeQuery = true)
    int reserverLaReponse(@Param("nom") String nom,
                          @Param("idParticipant") Long idParticipant,
                          @Param("indexMorceau") int indexMorceau);
}
