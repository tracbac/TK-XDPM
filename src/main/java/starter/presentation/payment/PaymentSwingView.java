package starter.presentation.payment;

import starter.domain.PaymentMethod;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class PaymentSwingView extends JFrame implements PaymentView {
    private final JComboBox<String> orderIdField = new JComboBox<>(
            new String[]{"PAY-ONLINE", "PAY-COD", "PAY-FAIL"});
    private final JTextField amountField = new JTextField("240000", 16);
    private final JComboBox<PaymentMethod> methodField =
            new JComboBox<>(PaymentMethod.values());
    private final JLabel resultLabel = new JLabel("Nhập thông tin thanh toán.");
    private final JTextArea explanationArea = new JTextArea(9, 56);

    public PaymentSwingView() {
        super("Thanh toán đơn hàng - Demo");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        methodField.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                setText(value instanceof PaymentMethod method
                        ? method.getDisplayName()
                        : String.valueOf(value));
                return this;
            }
        });

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        form.add(new JLabel("Mã đơn hàng"));
        form.add(orderIdField);
        form.add(new JLabel("Kịch bản mẫu"));
        form.add(new JLabel("ONLINE: thành công | COD: chờ thu | FAIL: cổng từ chối"));
        form.add(new JLabel("Số tiền (VNĐ)"));
        form.add(amountField);
        form.add(new JLabel("Phương thức"));
        form.add(methodField);

        JButton submitButton = new JButton("Thanh toán");
        submitButton.addActionListener(event -> Objects.requireNonNull(controller)
                .submitPayment(
                        (String) orderIdField.getSelectedItem(),
                        amountField.getText(),
                        (PaymentMethod) methodField.getSelectedItem()));

        JPanel result = new JPanel(new BorderLayout(4, 8));
        result.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
        explanationArea.setEditable(false);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setText("Chọn kịch bản để thấy quy trình và trạng thái đơn thay đổi.");
        JPanel outcome = new JPanel(new BorderLayout(4, 4));
        outcome.add(resultLabel, BorderLayout.NORTH);
        outcome.add(new JScrollPane(explanationArea), BorderLayout.CENTER);
        result.add(outcome, BorderLayout.CENTER);
        result.add(new JLabel(
                "DEMO: gateway mô phỏng, không trừ tiền; tên policy chỉ minh họa quy tắc được chọn."),
                BorderLayout.SOUTH);

        add(form, BorderLayout.CENTER);
        add(submitButton, BorderLayout.SOUTH);
        add(result, BorderLayout.NORTH);
        pack();
        setLocationRelativeTo(null);
    }

    private PaymentSwingController controller;

    public void setController(PaymentSwingController controller) {
        this.controller = controller;
    }

    @Override
    public void display(PaymentViewModel viewModel) {
        Runnable update = () -> {
            resultLabel.setText(viewModel.message());
            boolean cashOnDelivery =
                    viewModel.paymentMethod() == PaymentMethod.CASH_ON_DELIVERY;
            String oldStatus = "PENDING";
            String newStatus = switch (viewModel.status()) {
                case SUCCEEDED -> "PAID";
                case PENDING, FAILED -> "PENDING";
            };
            String gateway = viewModel.securityRule() == null
                    ? "không gọi (dữ liệu chưa hợp lệ)"
                    : cashOnDelivery
                            ? "không gọi (COD)"
                            : viewModel.status()
                                    == starter.domain.PaymentTransactionStatus.FAILED
                                    ? "đã gọi, giao dịch bị từ chối/lỗi"
                                    : "đã gọi mô phỏng";
            explanationArea.setText(String.format(
                    "Đơn hàng: %s%n"
                            + "Số tiền: %s VNĐ%n"
                            + "Phương thức: %s%n"
                            + "Trạng thái đơn: %s → %s%n"
                            + "Cổng thanh toán: %s%n"
                            + "Policy được chọn: %s%n"
                            + "Hóa đơn: %s%n"
                            + "Kết quả giao dịch: %s",
                    viewModel.orderId(),
                    viewModel.amount() == null ? "không hợp lệ" : viewModel.amount().toPlainString(),
                    viewModel.paymentMethod() == null
                            ? "—"
                            : viewModel.paymentMethod().getDisplayName(),
                    oldStatus,
                    newStatus,
                    gateway,
                    viewModel.securityRule() == null ? "chưa áp dụng" : viewModel.securityRule(),
                    viewModel.invoiceAction() == null ? "không phát hành" : viewModel.invoiceAction(),
                    viewModel.status()));
        };
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        } else {
            SwingUtilities.invokeLater(update);
        }
    }
}
