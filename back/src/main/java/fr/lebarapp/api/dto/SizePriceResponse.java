package fr.lebarapp.api.dto;

import fr.lebarapp.api.domain.Size;
import java.math.BigDecimal;

public record SizePriceResponse(
    Size size,
    BigDecimal price
) {}
