package fr.lebarapp.api.mapper;

import fr.lebarapp.api.domain.Ingredient;
import fr.lebarapp.api.dto.IngredientRequest;
import fr.lebarapp.api.dto.IngredientResponse;

// Convertit les ingrédients entre entités (base) et DTO (API), dans les deux sens.
public class IngredientMapper {

    private IngredientMapper() {
        throw new AssertionError("Utilitaire non instanciable");
    }

    public static Ingredient toEntity(IngredientRequest request) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(request.name());
        return ingredient;
    }

    public static void updateEntity(IngredientRequest request, Ingredient ingredient) {
        ingredient.setName(request.name());
    }

    public static IngredientResponse toResponse(Ingredient ingredient) {
        return new IngredientResponse(
            ingredient.getId(),
            ingredient.getName()
        );
    }
}
