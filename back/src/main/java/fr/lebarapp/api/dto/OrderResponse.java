package fr.lebarapp.api.dto;

import fr.lebarapp.api.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Commande renvoyée au client et au barmaker, avec son statut et son total calculé.
public record OrderResponse(
    Long id,
    Long tableId,
    String tableLabel,
    String customerName,
    OrderStatus status,
    LocalDateTime createdAt,
    List<OrderItemResponse> items,
    BigDecimal total
) {}
