package fr.lebarapp.api.external;

import java.util.List;

public record ExternalCocktailDto(
    String name,
    String category,
    String instructions,
    String imageUrl,
    List<ExternalIngredientDto> ingredients
) {}
