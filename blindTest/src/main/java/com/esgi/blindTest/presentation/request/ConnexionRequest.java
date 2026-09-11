package com.esgi.blindTest.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record ConnexionRequest(@NotBlank String email, @NotBlank String motDePasse) {
}
