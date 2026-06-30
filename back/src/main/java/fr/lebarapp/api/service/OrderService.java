package fr.lebarapp.api.service;

import fr.lebarapp.api.domain.BarTable;
import fr.lebarapp.api.domain.Cocktail;
import fr.lebarapp.api.domain.CocktailSize;
import fr.lebarapp.api.domain.Order;
import fr.lebarapp.api.domain.OrderItem;
import fr.lebarapp.api.domain.OrderStatus;
import fr.lebarapp.api.domain.PrepStatus;
import fr.lebarapp.api.domain.Size;
import fr.lebarapp.api.dto.OrderRequest;
import fr.lebarapp.api.dto.OrderResponse;
import fr.lebarapp.api.error.BusinessException;
import fr.lebarapp.api.error.ResourceNotFoundException;
import fr.lebarapp.api.mapper.OrderMapper;
import fr.lebarapp.api.repository.BarTableRepository;
import fr.lebarapp.api.repository.CocktailRepository;
import fr.lebarapp.api.repository.OrderItemRepository;
import fr.lebarapp.api.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final BarTableRepository barTableRepository;
    private final CocktailRepository cocktailRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(
        OrderRepository orderRepository,
        BarTableRepository barTableRepository,
        CocktailRepository cocktailRepository,
        OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.barTableRepository = barTableRepository;
        this.cocktailRepository = cocktailRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // Résoudre la table
        BarTable table = barTableRepository.findById(request.tableId())
            .orElseThrow(() -> new ResourceNotFoundException("Table introuvable avec l'ID: " + request.tableId()));

        Order order = new Order();
        order.setTable(table);
        order.setCustomerName(request.customerName());
        order.setStatus(OrderStatus.COMMANDEE);

        // Traiter les articles de commande
        for (var lineRequest : request.items()) {
            Cocktail cocktail = cocktailRepository.findById(lineRequest.cocktailId())
                .orElseThrow(() -> new ResourceNotFoundException("Cocktail introuvable avec l'ID: " + lineRequest.cocktailId()));

            // Récupérer le prix pour la taille demandée (snapshot)
            CocktailSize priceInfo = cocktail.getSizes().stream()
                .filter(cs -> cs.getSize() == lineRequest.size())
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                    "Le cocktail n'a pas la taille demandée: " + lineRequest.size()));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setCocktail(cocktail);
            item.setSize(lineRequest.size());
            item.setUnitPrice(priceInfo.getPrice()); // Snapshot du prix
            item.setPreparationStatus(PrepStatus.PREPARATION_INGREDIENTS);

            order.getItems().add(item);
        }

        order = orderRepository.save(order);
        return OrderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getQueue() {
        List<OrderStatus> statuses = Arrays.asList(OrderStatus.COMMANDEE, OrderStatus.EN_PREPARATION);
        return orderRepository.findByStatusInOrderByCreatedAtAsc(statuses).stream()
            .map(OrderMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable avec l'ID: " + id));
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse advanceItemPreparation(Long orderId, Long itemId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable avec l'ID: " + orderId));

        OrderItem item = orderItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Article introuvable avec l'ID: " + itemId));

        // Vérifier que l'item appartient bien à la commande
        if (!item.getOrder().getId().equals(orderId)) {
            throw new ResourceNotFoundException("L'article n'appartient pas à cette commande");
        }

        // Faire avancer l'étape
        PrepStatus currentStatus = item.getPreparationStatus();
        if (currentStatus == PrepStatus.TERMINEE) {
            throw new BusinessException("L'étape de préparation est déjà terminée");
        }

        item.setPreparationStatus(getNextStatus(currentStatus));
        orderItemRepository.save(item);

        // Recalculer le statut de la commande
        recalculateOrderStatus(order);
        order = orderRepository.save(order);

        return OrderMapper.toResponse(order);
    }

    private PrepStatus getNextStatus(PrepStatus current) {
        return switch (current) {
            case PREPARATION_INGREDIENTS -> PrepStatus.ASSEMBLAGE;
            case ASSEMBLAGE -> PrepStatus.DRESSAGE;
            case DRESSAGE -> PrepStatus.TERMINEE;
            case TERMINEE -> PrepStatus.TERMINEE;
        };
    }

    private void recalculateOrderStatus(Order order) {
        List<OrderItem> items = order.getItems();

        if (items.isEmpty()) {
            return;
        }

        // Si TOUS les items sont TERMINEE -> Order.status = TERMINEE
        if (items.stream().allMatch(item -> item.getPreparationStatus() == PrepStatus.TERMINEE)) {
            order.setStatus(OrderStatus.TERMINEE);
        }
        // Si AU MOINS un item a une étape différente de PREPARATION_INGREDIENTS -> EN_PREPARATION
        else if (items.stream().anyMatch(item -> item.getPreparationStatus() != PrepStatus.PREPARATION_INGREDIENTS)) {
            order.setStatus(OrderStatus.EN_PREPARATION);
        }
        // Sinon COMMANDEE (tous les items à PREPARATION_INGREDIENTS)
        else {
            order.setStatus(OrderStatus.COMMANDEE);
        }
    }
}
