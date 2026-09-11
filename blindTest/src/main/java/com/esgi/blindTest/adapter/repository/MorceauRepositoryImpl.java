package com.esgi.blindTest.adapter.repository;

import com.esgi.blindTest.adapter.mapper.MorceauEntityMapper;
import com.esgi.blindTest.domain.model.Morceau;
import com.esgi.blindTest.domain.repository.MorceauRepository;
import com.esgi.blindTest.infra.persistance.repository.MorceauJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@AllArgsConstructor
public class MorceauRepositoryImpl implements MorceauRepository {

    private final MorceauJpaRepository morceauJpaRepository;
    private final MorceauEntityMapper morceauEntityMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Morceau> findAll() {
        return morceauEntityMapper.toEntities(morceauJpaRepository.findAll());
    }
}
