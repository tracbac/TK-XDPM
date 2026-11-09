package starter.presentation.cancelorder;

import starter.domain.OrderStatus;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class CancelOrderSwingView extends JFrame implements CancelOrderView {
    private final JComboBox<String> orderIdField = new JComboBox<>(
            new String[]{"ORD-101", "ORD-104", "ORD-103", "ORD-105", "ORD-102", "ORD-999"});
    private final JTextField reasonField = new JTextField("Khách muốn đổi ý", 18);
    private final JLabel resultLabel = new JLabel("Nhập mã đơn và lý do để hủy.");
    private final JTextArea explanationArea = new JTextArea(8, 48);
    private CancelOrderSwingController controller;

    public CancelOrderSwingView() {
        super("Hủy đơn hàng - Demo");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        form.add(new JLabel("Mã đơn hàng"));
        form.add(orderIdField);
        form.add(new JLabel("Mã mẫu"));
        form.add(new JLabel("101/104: hủy được | 103/105: đang/đã giao | 102: đã hủy | 999: không tồn tại"));
        form.add(new JLabel("Lý do hủy"));
        form.add(reasonField);

        JButton cancelButton = new JButton("Hủy đơn hàng");
        cancelButton.addActionListener(event -> Objects.requireNonNull(controller).cancelOrder(
                (String) orderIdField.getSelectedItem(), reasonField.getText()));

        JPanel feedback = new JPanel(new BorderLayout(4, 8));
        feedback.setBorder(BorderFactory.createEmptyBorder(0, 16, 8, 16));
        explanationArea.setEditable(false);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setText("Chọn một đơn mẫu để xem điều kiện BR11 và tác động BR17/BR12.");
        JPanel outcome = new JPanel(new BorderLayout(4, 4));
        outcome.add(resultLabel, BorderLayout.NORTH);
        outcome.add(new JScrollPane(explanationArea), BorderLayout.CENTER);
        feedback.add(outcome, BorderLayout.CENTER);
        feedback.add(new JLabel("DEMO: đơn hàng, tồn kho và audit mô phỏng trong bộ nhớ."),
                BorderLayout.SOUTH);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
        footer.add(cancelButton, BorderLayout.EAST);

        add(form, BorderLayout.NORTH);
        add(feedback, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
        pack();
        setMinimumSize(new Dimension(560, getHeight()));
        setLocationRelativeTo(null);
    }

    public void setController(CancelOrderSwingController controller) {
        this.controller = controller;
    }

    @Override
    public void display(CancelOrderViewModel viewModel) {
        Runnable update = () -> {
            resultLabel.setText(viewModel.message());
            resultLabel.setForeground(viewModel.successful()
                    ? new Color(24, 120, 70)
                    : new Color(180, 45, 45));
            OrderStatus before = viewModel.previousStatus();
            OrderStatus after = viewModel.orderStatus();
            explanationArea.setText(String.format(
                    "Đơn hàng: %s%n"
                            + "Trạng thái: %s → %s%n"
                            + "Điều kiện BR11: %s%n"
                            + "Tồn kho BR17: %s%n"
                            + "Audit BR12: %s%n"
                            + "Lý do: %s",
                    viewModel.orderId(),
                    before == null ? "Không tìm thấy" : before,
                    after == null ? "Không đổi" : after,
                    viewModel.successful()
                            ? "đủ điều kiện hủy (PENDING/PROCESSING)"
                            : "bị từ chối: đơn không tồn tại hoặc trạng thái không cho phép",
                    viewModel.stockReleased() ? "đã gọi hoàn trả kho" : "không thay đổi",
                    viewModel.auditLogged() ? "đã ghi nhận" : "chưa ghi nhận",
                    viewModel.message()));
        };
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        } else {
            SwingUtilities.invokeLater(update);
        }
    }
}
