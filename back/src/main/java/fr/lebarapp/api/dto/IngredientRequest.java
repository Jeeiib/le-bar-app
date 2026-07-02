package fr.lebarapp.api.dto;

import jakarta.validation.constraints.NotBlank;

// Nom d'un ingrédient à créer.
public record IngredientRequest(
    @NotBlank(message = "Le nom de l'ingrédient ne peut pas être vide")
    String name
) {}
