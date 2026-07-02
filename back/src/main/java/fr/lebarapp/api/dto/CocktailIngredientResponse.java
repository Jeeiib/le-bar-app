package fr.lebarapp.api.dto;

// Un ingrédient d'un cocktail tel qu'affiché sur la carte.
public record CocktailIngredientResponse(
    String name,
    String measure
) {}
