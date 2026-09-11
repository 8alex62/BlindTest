package com.esgi.blindTest.adapter.mapper;

import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.Morceau;
import com.esgi.blindTest.domain.model.Participant;
import com.esgi.blindTest.domain.model.Participation;
import com.esgi.blindTest.presentation.response.BlindTestResponse;
import com.esgi.blindTest.presentation.response.EtatBlindTestResponse;
import com.esgi.blindTest.presentation.response.ScoreParticipant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Objects;

/**
 * Conversion du domaine vers les reponses de l'API. Aucune entite JPA ici.
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
        Morceau morceauCourant = blindTest.morceauCourant();
        Long reservataire = blindTest.getIdParticipantReservataire();
        return new EtatBlindTestResponse(
                blindTest.getId(),
                blindTest.getNom(),
                blindTest.getStatut(),
                blindTest.getEtatLecture(),
                blindTest.numeroDuMorceauCourant(),
                BlindTest.NOMBRE_DE_MORCEAUX,
                morceauCourant == null ? null : morceauCourant.getUrlAudio(),
                reservataire != null,
                participant != null && Objects.equals(reservataire, participant.getId()),
                blindTest.classement().stream().map(this::toScoreDto).toList());
    }
}
