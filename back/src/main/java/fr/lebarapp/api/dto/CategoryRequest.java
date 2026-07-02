package fr.lebarapp.api.dto;

import jakarta.validation.constraints.NotBlank;

// Données reçues pour créer ou renommer une catégorie de la carte.
public record CategoryRequest(
    @NotBlank(message = "Le nom de la catégorie ne peut pas être vide")
    String name,
    int position
) {}
