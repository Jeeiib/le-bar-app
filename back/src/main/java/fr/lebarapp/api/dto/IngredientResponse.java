package fr.lebarapp.api.dto;

// Un ingrédient renvoyé par l'API.
public record IngredientResponse(
    Long id,
    String name
) {}
