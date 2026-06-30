package fr.lebarapp.api.dto;

import java.util.List;

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
