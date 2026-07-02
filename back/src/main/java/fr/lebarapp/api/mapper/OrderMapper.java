package fr.lebarapp.api.mapper;

import fr.lebarapp.api.domain.Order;
import fr.lebarapp.api.domain.OrderItem;
import fr.lebarapp.api.dto.OrderItemResponse;
import fr.lebarapp.api.dto.OrderResponse;
import java.math.BigDecimal;
import java.util.List;

public class OrderMapper {

    private OrderMapper() {
        throw new AssertionError("Utilitaire non instanciable");
    }

    public static OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
            .map(OrderMapper::toItemResponse)
            .toList();

        BigDecimal total = order.getItems().stream()
            .map(OrderItem::getUnitPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderResponse(
            order.getId(),
            order.getTable().getId(),
            order.getTable().getLabel(),
            order.getCustomerName(),
            order.getStatus(),
            order.getCreatedAt(),
            itemResponses,
            total
        );
    }

    public static OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
            item.getId(),
            item.getCocktail().getId(),
            item.getCocktail().getName(),
            item.getSize(),
            item.getUnitPrice(),
            item.getPreparationStatus()
        );
    }
}
