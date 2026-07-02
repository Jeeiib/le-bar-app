package fr.lebarapp.api.repository;

import fr.lebarapp.api.domain.Order;
import fr.lebarapp.api.domain.OrderStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Requête dérivée : Spring Data JPA génère le SQL depuis le nom de la méthode
    // (WHERE status IN (...) ORDER BY created_at ASC). Utilisée pour la file du barmaker.
    List<Order> findByStatusInOrderByCreatedAtAsc(Collection<OrderStatus> statuses);
}
