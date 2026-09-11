package com.esgi.blindTest.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record AjoutBlindTestRequest(@NotBlank String nom) {
}
