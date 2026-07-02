package fr.lebarapp.api.dto;

import fr.lebarapp.api.domain.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

// Un couple taille + prix saisi pour un cocktail.
public record SizePriceRequest(
    @NotNull(message = "La taille ne peut pas être nulle")
    Size size,
    @NotNull(message = "Le prix ne peut pas être nul")
    @Positive(message = "Le prix doit être positif")
    BigDecimal price
) {}
