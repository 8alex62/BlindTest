package com.esgi.blindTest.adapter.mapper;

import com.esgi.blindTest.domain.model.Morceau;
import com.esgi.blindTest.infra.persistance.entity.MorceauEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface MorceauEntityMapper {

    Morceau toEntity(MorceauEntity morceauEntity);

    MorceauEntity toDto(Morceau morceau);

    List<Morceau> toEntities(List<MorceauEntity> morceauEntities);
}
