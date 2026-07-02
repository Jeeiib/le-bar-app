package fr.lebarapp.api.dto;

import fr.lebarapp.api.domain.Size;
import java.math.BigDecimal;

// Un couple taille + prix renvoyé pour un cocktail.
public record SizePriceResponse(
    Size size,
    BigDecimal price
) {}
