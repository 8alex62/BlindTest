package com.esgi.blindTest.domain.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Porteur de donnees. Identifie par son email, qui est unique.
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Participant {

    @NonNull
    @Email(message = "Merci d'indiquer un email valide")
    private String email;

    @NonNull
    // Regle metier : le mot de passe contient au moins 8 caracteres
    @Size(min = 8, message = "Votre mot de passe doit contenir au moins {min} caractères")
    private String motDePasse;
}
