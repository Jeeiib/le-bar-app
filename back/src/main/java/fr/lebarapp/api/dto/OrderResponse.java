package fr.lebarapp.api.dto;

import fr.lebarapp.api.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
