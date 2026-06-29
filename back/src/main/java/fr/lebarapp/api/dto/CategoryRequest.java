package fr.lebarapp.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
    @NotBlank(message = "Le nom de la catégorie ne peut pas être vide")
    String name,
    int position
) {}
