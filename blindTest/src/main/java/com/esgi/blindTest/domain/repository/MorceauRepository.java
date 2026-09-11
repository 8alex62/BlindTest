package com.esgi.blindTest.domain.repository;

import com.esgi.blindTest.domain.model.Morceau;

import java.util.List;

/**
 * Port de lecture du catalogue de morceaux.
 */
public interface MorceauRepository {

    List<Morceau> findAll();
}
