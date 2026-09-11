package com.esgi.blindTest.adapter.mapper;

import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.Morceau;
import com.esgi.blindTest.domain.model.Participant;
import com.esgi.blindTest.domain.model.Participation;
import com.esgi.blindTest.domain.model.StatutBlindTest;
import com.esgi.blindTest.presentation.response.BlindTestResponse;
import com.esgi.blindTest.presentation.response.EtatBlindTestResponse;
import com.esgi.blindTest.presentation.response.ScoreParticipant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.Comparator;
import java.util.List;

/**
 * Conversion du domaine vers les reponses de l'API. Aucune entite JPA ici.
 * Les modeles n'ayant plus de comportement, les valeurs d'affichage (morceau courant,
 * numero du morceau, classement) sont calculees ici : ce sont des besoins de presentation.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface BlindTestMapper {

    @Mapping(target = "nombreDeParticipants", expression = "java(blindTest.getParticipations().size())")
    @Mapping(target = "nombreMaximumDeParticipants",
            expression = "java(com.esgi.blindTest.domain.model.BlindTest.NOMBRE_MAXIMUM_DE_PARTICIPANTS)")
    BlindTestResponse toDto(BlindTest blindTest);

    List<BlindTestResponse> toDtos(List<BlindTest> blindTests);

    @Mapping(target = "email", source = "participant.email")
    ScoreParticipant toScoreDto(Participation participation);

    default EtatBlindTestResponse toEtatDto(BlindTest blindTest, Participant participant) {
        Morceau morceauCourant = morceauCourant(blindTest);
        Participant reservataire = blindTest.getReservataire();
        return new EtatBlindTestResponse(
                blindTest.getNom(),
                blindTest.getStatut(),
                blindTest.getEtatLecture(),
                Math.min(blindTest.getIndexMorceauCourant() + 1, BlindTest.NOMBRE_DE_MORCEAUX),
                BlindTest.NOMBRE_DE_MORCEAUX,
                BlindTest.NOMBRE_MAXIMUM_DE_PARTICIPANTS,
                morceauCourant == null ? null : morceauCourant.getUrlAudio(),
                // Sert a masquer le bouton "Rejoindre" et a montrer "Lancer le blind test".
                estInscrit(blindTest, participant),
                reservataire != null,
                participant != null && reservataire != null
                        && reservataire.getEmail().equals(participant.getEmail()),
                classement(blindTest));
    }

    private boolean estInscrit(BlindTest blindTest, Participant participant) {
        return participant != null && blindTest.getParticipations().stream()
                .anyMatch(participation -> participation.getParticipant().getEmail()
                        .equals(participant.getEmail()));
    }

    private Morceau morceauCourant(BlindTest blindTest) {
        if (blindTest.getStatut() != StatutBlindTest.EN_COURS
                || blindTest.getIndexMorceauCourant() >= blindTest.getMorceaux().size()) {
            return null;
        }
        return blindTest.getMorceaux().get(blindTest.getIndexMorceauCourant());
    }

    private List<ScoreParticipant> classement(BlindTest blindTest) {
        return blindTest.getParticipations().stream()
                .sorted(Comparator.comparingInt(Participation::getScore).reversed())
                .map(this::toScoreDto)
                .toList();
    }
}
