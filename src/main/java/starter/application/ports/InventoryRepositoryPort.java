package starter.application.ports;

import starter.domain.Order;

public interface InventoryRepositoryPort {
    void releaseStock(Order order);
}
