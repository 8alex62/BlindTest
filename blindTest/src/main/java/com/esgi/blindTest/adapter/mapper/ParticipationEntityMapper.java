package com.esgi.blindTest.adapter.mapper;

import com.esgi.blindTest.domain.model.Participation;
import com.esgi.blindTest.infra.persistance.entity.ParticipationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = ParticipantEntityMapper.class)
public interface ParticipationEntityMapper {

    // La reference inverse vers le blind test reste dans l'infrastructure.
    Participation toEntity(ParticipationEntity participationEntity);

    ParticipationEntity toDto(Participation participation);
}
