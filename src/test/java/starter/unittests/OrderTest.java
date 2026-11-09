package starter.unittests;

import org.junit.jupiter.api.Test;
import starter.domain.Order;
import starter.domain.OrderItem;
import starter.domain.OrderStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {
    @Test
    void copiesItemsAtConstructionAndDoesNotExposeMutableItems() {
        List<OrderItem> sourceItems = new ArrayList<>();
        sourceItems.add(new OrderItem("CAKE-1", 2));
        Order order = new Order("ORD-1", OrderStatus.PENDING, sourceItems);

        sourceItems.add(new OrderItem("CAKE-2", 1));

        assertEquals(1, order.getItems().size());
        assertThrows(UnsupportedOperationException.class,
                () -> order.getItems().add(new OrderItem("CAKE-3", 1)));
    }

    @Test
    void rejectsInvalidOrderData() {
        assertThrows(IllegalArgumentException.class,
                () -> new Order(" ", OrderStatus.PENDING, List.of()));
        assertThrows(NullPointerException.class,
                () -> new Order("ORD-1", null, List.of()));
        assertThrows(NullPointerException.class,
                () -> new Order("ORD-1", OrderStatus.PENDING, null));
        assertThrows(NullPointerException.class,
                () -> new Order("ORD-1", OrderStatus.PENDING, List.of((OrderItem) null)));
    }

    @Test
    void rejectsInvalidOrderItems() {
        assertThrows(IllegalArgumentException.class, () -> new OrderItem("", 1));
        assertThrows(IllegalArgumentException.class, () -> new OrderItem("CAKE-1", 0));
    }

    @Test
    void changesStatusWhenCancelledFromAnEligibleState() {
        Order order = new Order("ORD-1", OrderStatus.PROCESSING, List.of());

        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void cannotCancelAnOrderThatWasAlreadyShipped() {
        Order order = new Order("ORD-1", OrderStatus.SHIPPED, List.of());

        assertThrows(IllegalStateException.class, order::cancel);
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }
}
