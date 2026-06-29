package fr.lebarapp.api.web;

import fr.lebarapp.api.dto.OrderRequest;
import fr.lebarapp.api.dto.OrderResponse;
import fr.lebarapp.api.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/queue")
    public ResponseEntity<List<OrderResponse>> getQueue() {
        List<OrderResponse> orders = orderService.getQueue();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{orderId}/items/{itemId}/advance")
    public ResponseEntity<OrderResponse> advanceItemPreparation(
        @PathVariable Long orderId,
        @PathVariable Long itemId) {
        OrderResponse order = orderService.advanceItemPreparation(orderId, itemId);
        return ResponseEntity.ok(order);
    }
}
