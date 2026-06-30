package fr.lebarapp.api.mapper;

import fr.lebarapp.api.domain.Cocktail;
import fr.lebarapp.api.domain.CocktailSize;
import fr.lebarapp.api.dto.CocktailRequest;
import fr.lebarapp.api.dto.CocktailResponse;
import fr.lebarapp.api.dto.SizePriceRequest;
import fr.lebarapp.api.dto.SizePriceResponse;
import java.util.List;
import java.util.stream.Collectors;

public class CocktailMapper {

    private CocktailMapper() {
        throw new AssertionError("Utilitaire non instanciable");
    }

    public static Cocktail toEntity(CocktailRequest request) {
        Cocktail cocktail = new Cocktail();
        cocktail.setName(request.name());
        cocktail.setDescription(request.description());
        cocktail.setImageUrl(request.imageUrl());
        cocktail.setAvailable(request.available());
        return cocktail;
    }

    public static void updateEntity(CocktailRequest request, Cocktail cocktail) {
        cocktail.setName(request.name());
        cocktail.setDescription(request.description());
        cocktail.setImageUrl(request.imageUrl());
        cocktail.setAvailable(request.available());
    }

    public static CocktailResponse toResponse(Cocktail cocktail) {
        List<String> ingredientNames = cocktail.getIngredients().stream()
            .map(ing -> ing.getName())
            .collect(Collectors.toList());

        List<SizePriceResponse> sizePrices = cocktail.getSizes().stream()
            .map(cs -> new SizePriceResponse(cs.getSize(), cs.getPrice()))
            .collect(Collectors.toList());

        return new CocktailResponse(
            cocktail.getId(),
            cocktail.getName(),
            cocktail.getDescription(),
            cocktail.getImageUrl(),
            cocktail.isAvailable(),
            cocktail.getCategory().getId(),
            cocktail.getCategory().getName(),
            ingredientNames,
            sizePrices
        );
    }

    public static void updateSizes(Cocktail cocktail, List<SizePriceRequest> sizeRequests) {
        cocktail.getSizes().clear();
        for (SizePriceRequest request : sizeRequests) {
            CocktailSize size = new CocktailSize();
            size.setCocktail(cocktail);
            size.setSize(request.size());
            size.setPrice(request.price());
            cocktail.getSizes().add(size);
        }
    }
}
