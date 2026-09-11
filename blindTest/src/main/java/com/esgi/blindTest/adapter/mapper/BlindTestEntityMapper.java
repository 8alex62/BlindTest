package com.esgi.blindTest.adapter.mapper;

import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.infra.persistance.entity.BlindTestEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ParticipationEntityMapper.class, MorceauEntityMapper.class})
public interface BlindTestEntityMapper {

    BlindTest toEntity(BlindTestEntity blindTestEntity);

    // Les participations sont ecrites par ParticipationRepositoryImpl, jamais en cascade.
    @Mapping(target = "participations", ignore = true)
    BlindTestEntity toDto(BlindTest blindTest);

    /**
     * Reporte sur l'entite geree les seuls champs que le domaine fait evoluer.
     * L'identifiant, la version, les morceaux et les participations sont hors du jeu.
     * Les valeurs nulles sont recopiees volontairement : liberer la main consiste
     * justement a remettre le reservataire a null.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "morceaux", ignore = true)
    @Mapping(target = "participations", ignore = true)
    void mettreAJour(BlindTest blindTest, @MappingTarget BlindTestEntity blindTestEntity);
}
