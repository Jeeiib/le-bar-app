package fr.lebarapp.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import fr.lebarapp.api.domain.*;
import fr.lebarapp.api.dto.OrderLineRequest;
import fr.lebarapp.api.dto.OrderRequest;
import fr.lebarapp.api.dto.OrderResponse;
import fr.lebarapp.api.error.BusinessException;
import fr.lebarapp.api.error.ResourceNotFoundException;
import fr.lebarapp.api.repository.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

  @Mock private OrderRepository orderRepository;
  @Mock private BarTableRepository barTableRepository;
  @Mock private CocktailRepository cocktailRepository;
  @Mock private OrderItemRepository orderItemRepository;

  @InjectMocks private OrderService orderService;

  private BarTable mockTable;
  private Cocktail mockCocktail;
  private Order mockOrder;
  private OrderItem mockOrderItem;

  @BeforeEach
  void setUp() {
    mockTable = new BarTable();
    mockTable.setId(1L);
    mockTable.setLabel("Table 5");
    mockTable.setQrSlug("table-5-slug");

    mockCocktail = new Cocktail();
    mockCocktail.setId(1L);
    mockCocktail.setName("Mojito");

    CocktailSize size = new CocktailSize();
    size.setSize(Size.S);
    size.setPrice(new BigDecimal("5.00"));
    mockCocktail.setSizes(new ArrayList<>(List.of(size)));

    mockOrder = new Order();
    mockOrder.setId(1L);
    mockOrder.setTable(mockTable);
    mockOrder.setStatus(OrderStatus.COMMANDEE);
    mockOrder.setItems(new ArrayList<>());

    mockOrderItem = new OrderItem();
    mockOrderItem.setId(1L);
    mockOrderItem.setOrder(mockOrder);
    mockOrderItem.setCocktail(mockCocktail);
    mockOrderItem.setSize(Size.S);
    mockOrderItem.setUnitPrice(new BigDecimal("5.00"));
    mockOrderItem.setPreparationStatus(PrepStatus.PREPARATION_INGREDIENTS);
  }

  @Test
  @DisplayName("createOrder should create order with snapshot price")
  void testCreateOrder_Success() {
    OrderLineRequest lineRequest = new OrderLineRequest(1L, Size.S);
    OrderRequest request = new OrderRequest(1L, "John", List.of(lineRequest));

    when(barTableRepository.findById(1L)).thenReturn(Optional.of(mockTable));
    when(cocktailRepository.findById(1L)).thenReturn(Optional.of(mockCocktail));
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
      Order order = invocation.getArgument(0);
      order.setId(1L);
      return order;
    });

    OrderResponse response = orderService.createOrder(request);

    assertNotNull(response);
    assertEquals("John", response.customerName());
    assertEquals(OrderStatus.COMMANDEE, response.status());
    verify(orderRepository).save(any(Order.class));
  }

  @Test
  @DisplayName("createOrder should throw ResourceNotFoundException when table not found")
  void testCreateOrder_TableNotFound() {
    OrderLineRequest lineRequest = new OrderLineRequest(1L, Size.S);
    OrderRequest request = new OrderRequest(999L, "John", List.of(lineRequest));

    when(barTableRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(request));
  }

  @Test
  @DisplayName("createOrder should throw ResourceNotFoundException when cocktail not found")
  void testCreateOrder_CocktailNotFound() {
    OrderLineRequest lineRequest = new OrderLineRequest(999L, Size.S);
    OrderRequest request = new OrderRequest(1L, "John", List.of(lineRequest));

    when(barTableRepository.findById(1L)).thenReturn(Optional.of(mockTable));
    when(cocktailRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(request));
  }

  @Test
  @DisplayName("createOrder should throw BusinessException when size not available")
  void testCreateOrder_SizeNotAvailable() {
    OrderLineRequest lineRequest = new OrderLineRequest(1L, Size.L);
    OrderRequest request = new OrderRequest(1L, "John", List.of(lineRequest));

    when(barTableRepository.findById(1L)).thenReturn(Optional.of(mockTable));
    when(cocktailRepository.findById(1L)).thenReturn(Optional.of(mockCocktail));

    assertThrows(BusinessException.class, () -> orderService.createOrder(request));
  }

  @Test
  @DisplayName("advanceItemPreparation should advance status from PREPARATION_INGREDIENTS to ASSEMBLAGE")
  void testAdvanceItemPreparation_FromPreparationToAssemblage() {
    mockOrder.getItems().add(mockOrderItem);

    when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
    when(orderItemRepository.findById(1L)).thenReturn(Optional.of(mockOrderItem));
    when(orderItemRepository.save(any(OrderItem.class))).thenReturn(mockOrderItem);
    when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

    OrderResponse response = orderService.advanceItemPreparation(1L, 1L);

    assertNotNull(response);
    assertEquals(PrepStatus.ASSEMBLAGE, mockOrderItem.getPreparationStatus());
    verify(orderItemRepository).save(mockOrderItem);
  }

  @Test
  @DisplayName("advanceItemPreparation should throw BusinessException when item already TERMINEE")
  void testAdvanceItemPreparation_AlreadyTerminee() {
    mockOrderItem.setPreparationStatus(PrepStatus.TERMINEE);
    mockOrder.getItems().add(mockOrderItem);

    when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
    when(orderItemRepository.findById(1L)).thenReturn(Optional.of(mockOrderItem));

    assertThrows(BusinessException.class, () -> orderService.advanceItemPreparation(1L, 1L));
  }

  @Test
  @DisplayName("advanceItemPreparation should throw ResourceNotFoundException when item not found")
  void testAdvanceItemPreparation_ItemNotFound() {
    when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
    when(orderItemRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> orderService.advanceItemPreparation(1L, 999L));
  }

  @Test
  @DisplayName("advanceItemPreparation should throw ResourceNotFoundException when item doesn't belong to order")
  void testAdvanceItemPreparation_ItemNotBelongingToOrder() {
    Order anotherOrder = new Order();
    anotherOrder.setId(2L);
    mockOrderItem.setOrder(anotherOrder);

    when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
    when(orderItemRepository.findById(1L)).thenReturn(Optional.of(mockOrderItem));

    assertThrows(ResourceNotFoundException.class, () -> orderService.advanceItemPreparation(1L, 1L));
  }

  @Test
  @DisplayName("advanceItemPreparation should advance all statuses correctly")
  void testAdvanceItemPreparation_AllTransitions() {
    mockOrder.getItems().add(mockOrderItem);

    when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
    when(orderItemRepository.findById(1L)).thenReturn(Optional.of(mockOrderItem));
    when(orderItemRepository.save(any(OrderItem.class))).thenReturn(mockOrderItem);
    when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

    orderService.advanceItemPreparation(1L, 1L);
    assertEquals(PrepStatus.ASSEMBLAGE, mockOrderItem.getPreparationStatus());

    orderService.advanceItemPreparation(1L, 1L);
    assertEquals(PrepStatus.DRESSAGE, mockOrderItem.getPreparationStatus());

    orderService.advanceItemPreparation(1L, 1L);
    assertEquals(PrepStatus.TERMINEE, mockOrderItem.getPreparationStatus());
  }

  @Test
  @DisplayName("recalculateOrderStatus should set status to TERMINEE when all items are done")
  void testRecalculateOrderStatus_AllTerminee() {
    // Advance from PREPARATION_INGREDIENTS -> ASSEMBLAGE -> DRESSAGE -> TERMINEE
    mockOrder.getItems().add(mockOrderItem);

    when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
    when(orderItemRepository.findById(1L)).thenReturn(Optional.of(mockOrderItem));
    when(orderItemRepository.save(any(OrderItem.class))).thenReturn(mockOrderItem);
    when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

    // First advance: PREPARATION_INGREDIENTS -> ASSEMBLAGE
    orderService.advanceItemPreparation(1L, 1L);
    assertEquals(PrepStatus.ASSEMBLAGE, mockOrderItem.getPreparationStatus());

    // Second advance: ASSEMBLAGE -> DRESSAGE
    orderService.advanceItemPreparation(1L, 1L);
    assertEquals(PrepStatus.DRESSAGE, mockOrderItem.getPreparationStatus());

    // Third advance: DRESSAGE -> TERMINEE
    orderService.advanceItemPreparation(1L, 1L);
    assertEquals(PrepStatus.TERMINEE, mockOrderItem.getPreparationStatus());
    assertEquals(OrderStatus.TERMINEE, mockOrder.getStatus());
  }

  @Test
  @DisplayName("recalculateOrderStatus should set status to EN_PREPARATION when at least one item advanced")
  void testRecalculateOrderStatus_AtLeastOneAdvanced() {
    mockOrderItem.setPreparationStatus(PrepStatus.ASSEMBLAGE);
    mockOrder.getItems().add(mockOrderItem);

    when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
    when(orderItemRepository.findById(1L)).thenReturn(Optional.of(mockOrderItem));
    when(orderItemRepository.save(any(OrderItem.class))).thenReturn(mockOrderItem);
    when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

    orderService.advanceItemPreparation(1L, 1L);

    assertEquals(OrderStatus.EN_PREPARATION, mockOrder.getStatus());
  }

  @Test
  @DisplayName("getOrderById should return order when found")
  void testGetOrderById_Success() {
    when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

    OrderResponse response = orderService.getOrderById(1L);

    assertNotNull(response);
    verify(orderRepository).findById(1L);
  }

  @Test
  @DisplayName("getOrderById should throw ResourceNotFoundException when not found")
  void testGetOrderById_NotFound() {
    when(orderRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(999L));
  }

  @Test
  @DisplayName("getQueue should return orders with COMMANDEE or EN_PREPARATION status")
  void testGetQueue_Success() {
    List<Order> queueOrders = List.of(mockOrder);

    when(orderRepository.findByStatusInOrderByCreatedAtAsc(
            eq(Arrays.asList(OrderStatus.COMMANDEE, OrderStatus.EN_PREPARATION))))
        .thenReturn(queueOrders);

    List<OrderResponse> response = orderService.getQueue();

    assertNotNull(response);
    assertEquals(1, response.size());
    verify(orderRepository).findByStatusInOrderByCreatedAtAsc(any());
  }
}
