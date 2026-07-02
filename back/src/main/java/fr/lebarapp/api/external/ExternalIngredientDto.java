package fr.lebarapp.api.external;

// Un ingrédient (nom + mesure) issu de TheCocktailDB.
public record ExternalIngredientDto(
    String name,
    String measure
) {}
