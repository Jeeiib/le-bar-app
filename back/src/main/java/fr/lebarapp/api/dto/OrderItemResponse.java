package fr.lebarapp.api.dto;

import fr.lebarapp.api.domain.PrepStatus;
import fr.lebarapp.api.domain.Size;
import java.math.BigDecimal;

// Un article d'une commande, avec son prix figé au moment de l'achat et son étape de préparation.
public record OrderItemResponse(
    Long id,
    Long cocktailId,
    String cocktailName,
    Size size,
    BigDecimal unitPrice,
    PrepStatus preparationStatus
) {}
