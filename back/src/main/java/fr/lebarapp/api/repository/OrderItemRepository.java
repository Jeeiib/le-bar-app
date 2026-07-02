package fr.lebarapp.api.repository;

import fr.lebarapp.api.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

// Accès aux articles de commande : le CRUD hérité de JpaRepository suffit ici.
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
