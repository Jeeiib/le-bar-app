package fr.lebarapp.api.dto;

import java.util.List;

// Cocktail renvoyé par l'API : la carte lisible avec ses ingrédients, ses tailles et ses prix.
public record CocktailResponse(
    Long id,
    String name,
    String description,
    String imageUrl,
    boolean available,
    Long categoryId,
    String categoryName,
    List<CocktailIngredientResponse> ingredients,
    List<SizePriceResponse> sizes
) {}
