package fr.lebarapp.api.dto;

import fr.lebarapp.api.domain.PrepStatus;
import fr.lebarapp.api.domain.Size;
import java.math.BigDecimal;

public record OrderItemResponse(
    Long id,
    Long cocktailId,
    String cocktailName,
    Size size,
    BigDecimal unitPrice,
    PrepStatus preparationStatus
) {}
