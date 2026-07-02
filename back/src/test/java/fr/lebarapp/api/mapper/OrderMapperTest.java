package fr.lebarapp.api.mapper;

import static org.junit.jupiter.api.Assertions.*;

import fr.lebarapp.api.domain.*;
import fr.lebarapp.api.dto.OrderItemResponse;
import fr.lebarapp.api.dto.OrderResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OrderMapper Tests")
class OrderMapperTest {

  private Order mockOrder;

  @BeforeEach
  void setUp() {
    mockOrder = new Order();
    mockOrder.setId(1L);
    mockOrder.setCustomerName("John Doe");
    mockOrder.setStatus(OrderStatus.ORDERED);
    mockOrder.setItems(new ArrayList<>());

    BarTable table = new BarTable();
    table.setId(1L);
    table.setLabel("Table 5");
    mockOrder.setTable(table);
  }

  @Test
  @DisplayName("toResponse should convert Order to OrderResponse")
  void testToResponse() {
    OrderItem item = new OrderItem();
    item.setId(1L);
    item.setSize(Size.S);
    item.setUnitPrice(new BigDecimal("5.00"));
    item.setPreparationStatus(PrepStatus.INGREDIENTS);

    Cocktail cocktail = new Cocktail();
    cocktail.setId(1L);
    cocktail.setName("Mojito");
    item.setCocktail(cocktail);
    item.setOrder(mockOrder);

    mockOrder.getItems().add(item);

    OrderResponse response = OrderMapper.toResponse(mockOrder);

    assertNotNull(response);
    assertEquals(1L, response.id());
    assertEquals("John Doe", response.customerName());
    assertEquals(OrderStatus.ORDERED, response.status());
    assertNotNull(response.items());
    assertEquals(1, response.items().size());
  }

  @Test
  @DisplayName("toResponse should handle empty items list")
  void testToResponse_EmptyItems() {
    OrderResponse response = OrderMapper.toResponse(mockOrder);

    assertNotNull(response);
    assertEquals(1L, response.id());
    assertEquals("John Doe", response.customerName());
    assertNotNull(response.items());
    assertEquals(0, response.items().size());
  }

  @Test
  @DisplayName("OrderItemResponse should contain correct item details")
  void testOrderItemResponse_Details() {
    OrderItem item = new OrderItem();
    item.setId(1L);
    item.setSize(Size.M);
    item.setUnitPrice(new BigDecimal("7.50"));
    item.setPreparationStatus(PrepStatus.ASSEMBLY);

    Cocktail cocktail = new Cocktail();
    cocktail.setId(2L);
    cocktail.setName("Margarita");
    item.setCocktail(cocktail);
    item.setOrder(mockOrder);

    mockOrder.getItems().add(item);

    OrderResponse response = OrderMapper.toResponse(mockOrder);
    OrderItemResponse itemResponse = response.items().get(0);

    assertEquals(1L, itemResponse.id());
    assertEquals("Margarita", itemResponse.cocktailName());
    assertEquals(Size.M, itemResponse.size());
    assertEquals(new BigDecimal("7.50"), itemResponse.unitPrice());
    assertEquals(PrepStatus.ASSEMBLY, itemResponse.preparationStatus());
  }
}
