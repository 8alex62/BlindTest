package com.esgi.blindTest.presentation.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InscriptionRequest(@NotBlank @Email String email,
                                 @NotBlank @Size(min = 8) String motDePasse) {
}
