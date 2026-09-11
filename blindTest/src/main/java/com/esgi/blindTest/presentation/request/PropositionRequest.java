package com.esgi.blindTest.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record PropositionRequest(@NotBlank String proposition) {
}
