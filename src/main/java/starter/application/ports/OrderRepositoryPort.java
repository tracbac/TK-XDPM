package starter.application.ports;

import starter.domain.Order;

import java.util.Optional;

public interface OrderRepositoryPort {
    Optional<Order> findById(String orderId);

    void save(Order order);
}
