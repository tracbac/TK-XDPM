package starter.adapters.demo;

import starter.application.ports.InventoryRepositoryPort;
import starter.domain.Order;

public class ConsoleInventoryAdapter implements InventoryRepositoryPort {
    @Override
    public void releaseStock(Order order) {
        System.out.printf("DEMO inventory released for order %s%n", order.getId());
    }
}
