package com.esgi.blindTest.domain.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Participant {

    // Champ technique : identité persistante, utilisée pour désigner le réservataire d'une réponse.
    private Long id;

    @NonNull
    @Email(message = "Merci d'indiquer un email valide")
    private String email;

    @NonNull
    // Règle métier : le mot de passe contient au moins 8 caractères
    @Size(min = 8, message = "Votre mot de passe doit contenir au moins {min} caractères")
    private String motDePasse;
}
