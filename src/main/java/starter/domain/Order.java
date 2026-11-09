package starter.domain;

import java.util.List;
import java.util.Objects;

public class Order {
    private final String id;
    private OrderStatus status;
    private final List<OrderItem> items;

    public Order(String id, OrderStatus status, List<OrderItem> items) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Mã đơn hàng không được để trống.");
        }
        this.id = id;
        this.status = Objects.requireNonNull(status, "Trạng thái đơn hàng không được để trống.");
        this.items = List.copyOf(Objects.requireNonNull(items, "Danh sách sản phẩm không được để trống."));
    }

    // Quy tắc nghiệp vụ [BR11]: Kiểm tra xem đơn đã xuất kho giao shipper chưa
    public boolean isShipped() {
        return this.status == OrderStatus.SHIPPED;
    }

    // Thực hiện hủy đơn hàng
    public void cancel() {
        if (this.status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Đơn hàng đã được hủy trước đó.");
        }

        if (this.status == OrderStatus.SHIPPED || this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException(
                    "[BR11] Không thể hủy đơn hàng khi đang giao hoặc đã giao thành công.");
        }

        if (this.status != OrderStatus.PENDING && this.status != OrderStatus.PROCESSING) {
            throw new IllegalStateException(
                    "[BR11] Không thể hủy đơn hàng ở trạng thái " + this.status + ".");
        }

        this.status = OrderStatus.CANCELLED;
    }

    public void ensureCanBePaid() {
        if (this.status != OrderStatus.PENDING && this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Không thể thanh toán đơn hàng ở trạng thái " + this.status + ".");
        }
    }

    public void markPaid() {
        ensureCanBePaid();
        this.status = OrderStatus.PAID;
    }

    public String getId() {
        return id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}