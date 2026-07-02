package fr.lebarapp.api.dto;

import jakarta.validation.constraints.NotBlank;

// Un ingrédient saisi dans une recette, avec sa mesure (ex : "4 cl").
public record CocktailIngredientRequest(
    @NotBlank(message = "Le nom de l'ingrédient ne peut pas être vide")
    String name,
    String measure
) {}
