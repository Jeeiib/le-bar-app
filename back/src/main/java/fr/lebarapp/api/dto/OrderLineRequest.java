package fr.lebarapp.api.dto;

import fr.lebarapp.api.domain.Size;
import jakarta.validation.constraints.NotNull;

public record OrderLineRequest(
    @NotNull(message = "L'identifiant du cocktail ne peut pas être nul")
    Long cocktailId,
    @NotNull(message = "La taille ne peut pas être nulle")
    Size size
) {}
