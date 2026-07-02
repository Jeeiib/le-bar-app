package fr.lebarapp.api.external;

import java.util.List;

// Un cocktail tel que renvoyé par TheCocktailDB, avant transformation et enregistrement en base.
public record ExternalCocktailDto(
    String name,
    String category,
    String instructions,
    String imageUrl,
    List<ExternalIngredientDto> ingredients
) {}
