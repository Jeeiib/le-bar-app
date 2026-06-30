package fr.lebarapp.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

public record CocktailRequest(
    @NotBlank(message = "Le nom du cocktail ne peut pas être vide")
    String name,
    String description,
    String imageUrl,
    boolean available,
    @NotNull(message = "L'identifiant de catégorie ne peut pas être nul")
    Long categoryId,
    Set<Long> ingredientIds,
    @NotEmpty(message = "Au moins une taille et un prix doivent être fournis")
    @Valid
    List<SizePriceRequest> sizes
) {}
