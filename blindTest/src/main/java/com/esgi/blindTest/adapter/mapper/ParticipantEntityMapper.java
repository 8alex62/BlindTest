package com.esgi.blindTest.adapter.mapper;

import com.esgi.blindTest.domain.model.Participant;
import com.esgi.blindTest.infra.persistance.entity.ParticipantEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Convention du projet de reference : toEntity produit le modele du domaine,
 * toDto produit l'entite de persistance.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface ParticipantEntityMapper {

    Participant toEntity(ParticipantEntity participantEntity);

    ParticipantEntity toDto(Participant participant);
}
