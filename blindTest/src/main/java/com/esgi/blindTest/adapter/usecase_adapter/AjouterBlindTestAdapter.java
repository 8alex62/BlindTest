package com.esgi.blindTest.adapter.usecase_adapter;

import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.Morceau;
import com.esgi.blindTest.domain.repository.BlindTestRepository;
import com.esgi.blindTest.domain.repository.MorceauRepository;
import com.esgi.blindTest.domain.usecase.AjouterBlindTestUseCase;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@AllArgsConstructor
public class AjouterBlindTestAdapter implements AjouterBlindTestUseCase.OutputPort {

    private final BlindTestRepository blindTestRepository;
    private final MorceauRepository morceauRepository;

    @Override
    public List<Morceau> tirerLesMorceaux(int nombre) {
        List<Morceau> catalogue = new ArrayList<>(morceauRepository.findAll());
        Collections.shuffle(catalogue);
        return catalogue.stream().limit(nombre).toList();
    }

    @Override
    public BlindTest save(BlindTest blindTest) {
        return blindTestRepository.save(blindTest);
    }
}
