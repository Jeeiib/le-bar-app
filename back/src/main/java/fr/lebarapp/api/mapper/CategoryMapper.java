package fr.lebarapp.api.mapper;

import fr.lebarapp.api.domain.Category;
import fr.lebarapp.api.dto.CategoryRequest;
import fr.lebarapp.api.dto.CategoryResponse;

// Convertit les catégories entre entités (base) et DTO (API), dans les deux sens.
public class CategoryMapper {

    private CategoryMapper() {
        throw new AssertionError("Utilitaire non instanciable");
    }

    public static Category toEntity(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        category.setPosition(request.position());
        return category;
    }

    public static void updateEntity(CategoryRequest request, Category category) {
        category.setName(request.name());
        category.setPosition(request.position());
    }

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getPosition()
        );
    }
}
