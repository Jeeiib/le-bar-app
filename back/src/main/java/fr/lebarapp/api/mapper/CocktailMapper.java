package fr.lebarapp.api.mapper;

import fr.lebarapp.api.domain.Cocktail;
import fr.lebarapp.api.domain.CocktailIngredient;
import fr.lebarapp.api.domain.CocktailSize;
import fr.lebarapp.api.dto.CocktailIngredientResponse;
import fr.lebarapp.api.dto.CocktailRequest;
import fr.lebarapp.api.dto.CocktailResponse;
import fr.lebarapp.api.dto.SizePriceRequest;
import fr.lebarapp.api.dto.SizePriceResponse;
import java.util.List;

// Convertit les cocktails entre entités (base) et DTO (API), dans les deux sens.
public class CocktailMapper {

    private CocktailMapper() {
        throw new AssertionError("Utilitaire non instanciable");
    }

    public static Cocktail toEntity(CocktailRequest request) {
        Cocktail cocktail = new Cocktail();
        cocktail.setName(request.name());
        cocktail.setDescription(request.description());
        cocktail.setAvailable(request.available());
        return cocktail;
    }

    public static void updateEntity(CocktailRequest request, Cocktail cocktail) {
        cocktail.setName(request.name());
        cocktail.setDescription(request.description());
        cocktail.setAvailable(request.available());
    }

    public static CocktailResponse toResponse(Cocktail cocktail) {
        List<CocktailIngredientResponse> ingredientResponses = cocktail.getIngredients().stream()
            .map(ci -> new CocktailIngredientResponse(ci.getIngredient().getName(), ci.getMeasure()))
            .toList();

        List<SizePriceResponse> sizePrices = cocktail.getSizes().stream()
            .map(cs -> new SizePriceResponse(cs.getSize(), cs.getPrice()))
            .toList();

        String imageUrl = cocktail.getId() != null ? "/api/cocktails/" + cocktail.getId() + "/image" : null;

        return new CocktailResponse(
            cocktail.getId(),
            cocktail.getName(),
            cocktail.getDescription(),
            imageUrl,
            cocktail.isAvailable(),
            cocktail.getCategory().getId(),
            cocktail.getCategory().getName(),
            ingredientResponses,
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
