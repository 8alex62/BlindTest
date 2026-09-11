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
        uses = {ParticipationEntityMapper.class, MorceauEntityMapper.class,
                ParticipantEntityMapper.class})
public interface BlindTestEntityMapper {

    BlindTest toEntity(BlindTestEntity blindTestEntity);

    /**
     * Les morceaux, le reservataire et les participations sont volontairement ignores :
     * les modeles du domaine n'ont pas d'identifiant, MapStruct fabriquerait donc des
     * entites transitoires qu'Hibernate tenterait d'inserer. BlindTestRepositoryImpl
     * rattache lui-meme les entites gerees, retrouvees par leur cle naturelle.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "participations", ignore = true)
    @Mapping(target = "morceaux", ignore = true)
    @Mapping(target = "reservataire", ignore = true)
    BlindTestEntity toDto(BlindTest blindTest);

    /**
     * Reporte sur l'entite geree les seuls champs que les use cases font evoluer.
     * Les valeurs nulles sont recopiees volontairement : liberer la main consiste
     * justement a remettre le reservataire a null.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "morceaux", ignore = true)
    @Mapping(target = "participations", ignore = true)
    @Mapping(target = "reservataire", ignore = true)
    void mettreAJour(BlindTest blindTest, @MappingTarget BlindTestEntity blindTestEntity);
}
