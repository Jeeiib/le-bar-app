package fr.lebarapp.api.repository;

import fr.lebarapp.api.domain.Order;
import fr.lebarapp.api.domain.OrderStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatusInOrderByCreatedAtAsc(Collection<OrderStatus> statuses);
}
