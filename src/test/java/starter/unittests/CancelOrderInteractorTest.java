package starter.unittests;

import org.junit.jupiter.api.Test;
import starter.application.cancelorder.CancelOrderRequest;
import starter.application.cancelorder.CancelOrderResponse;
import starter.application.cancelorder.CancelOrderUseCase;
import starter.application.ports.AuditLogRepositoryPort;
import starter.application.ports.InventoryRepositoryPort;
import starter.application.ports.OrderRepositoryPort;
import starter.domain.Order;
import starter.domain.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CancelOrderInteractorTest {
    @Test
    void cancelsEligibleOrderAndReleasesStockAndAuditsTheAction() {
        Order order = new Order("ORD-1", OrderStatus.PENDING, List.of());
        AtomicBoolean stockReleased = new AtomicBoolean();
        AtomicBoolean auditLogged = new AtomicBoolean();
        AtomicReference<CancelOrderResponse> response = new AtomicReference<>();
        OrderRepositoryPort orders = new OrderRepositoryPort() {
            @Override
            public Optional<Order> findById(String orderId) {
                return Optional.of(order);
            }

            @Override
            public void save(Order savedOrder) {
                assertEquals(order, savedOrder);
            }
        };
        InventoryRepositoryPort inventory = ignored -> stockReleased.set(true);
        AuditLogRepositoryPort audit = (action, details) -> {
            assertEquals("CANCEL_ORDER", action);
            assertTrue(details.contains("customer request"));
            auditLogged.set(true);
        };

        new CancelOrderUseCase(orders, inventory, audit, response::set)
                .execute(new CancelOrderRequest("ORD-1", "customer request"));

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertTrue(stockReleased.get());
        assertTrue(auditLogged.get());
        assertNotNull(response.get());
        assertTrue(response.get().successful());
        assertEquals(OrderStatus.PENDING, response.get().previousStatus());
        assertEquals(OrderStatus.CANCELLED, response.get().orderStatus());
        assertTrue(response.get().stockReleased());
        assertTrue(response.get().auditLogged());
    }

    @Test
    void refusesToCancelMissingOrderAndRecordsTheFailure() {
        AtomicBoolean auditLogged = new AtomicBoolean();
        AtomicReference<CancelOrderResponse> response = new AtomicReference<>();
        OrderRepositoryPort orders = new OrderRepositoryPort() {
            @Override
            public Optional<Order> findById(String orderId) {
                return Optional.empty();
            }

            @Override
            public void save(Order order) {
                throw new AssertionError("A missing order must not be saved.");
            }
        };
        AuditLogRepositoryPort audit = (action, details) -> auditLogged.set(true);

        new CancelOrderUseCase(orders, ignored -> {
            throw new AssertionError("Stock must not be released for a missing order.");
        }, audit, response::set).execute(new CancelOrderRequest("ORD-404", "customer request"));

        assertTrue(auditLogged.get());
        assertNotNull(response.get());
        assertFalse(response.get().successful());
        assertEquals("ORD-404", response.get().orderId());
        assertFalse(response.get().stockReleased());
        assertTrue(response.get().auditLogged());
        assertTrue(response.get().message().contains("Không tìm thấy đơn hàng"));
    }
}
