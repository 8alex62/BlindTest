package com.esgi.blindTest.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Porteur de donnees. Identifiee par le couple blind test et participant.
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Participation {

    @NonNull
    private Participant participant;

    private int score;
}
