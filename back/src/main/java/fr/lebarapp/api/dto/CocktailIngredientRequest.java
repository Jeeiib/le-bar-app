package fr.lebarapp.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CocktailIngredientRequest(
    @NotBlank(message = "Le nom de l'ingrédient ne peut pas être vide")
    String name,
    String measure
) {}
