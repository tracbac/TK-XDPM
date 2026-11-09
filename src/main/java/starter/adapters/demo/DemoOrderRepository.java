package starter.adapters.demo;

import starter.application.ports.OrderRepositoryPort;
import starter.domain.Order;
import starter.domain.OrderStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DemoOrderRepository implements OrderRepositoryPort {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public DemoOrderRepository() {
        add("PAY-ONLINE", OrderStatus.PENDING);
        add("PAY-COD", OrderStatus.PENDING);
        add("PAY-FAIL", OrderStatus.PENDING);
        add("PAY-DEMO", OrderStatus.PENDING);
        add("ORD-101", OrderStatus.PENDING);
        add("ORD-104", OrderStatus.PROCESSING);
        add("ORD-103", OrderStatus.SHIPPED);
        add("ORD-105", OrderStatus.DELIVERED);
        add("ORD-102", OrderStatus.CANCELLED);
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public void save(Order order) {
        orders.put(order.getId(), order);
    }

    private void add(String orderId, OrderStatus status) {
        orders.put(orderId, new Order(orderId, status, List.of()));
    }
}
