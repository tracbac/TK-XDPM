Feature: Thanh toán tiền và hóa đơn mua bánh kẹo
  Như một khách hàng hoàn tất đơn hàng
  Tôi muốn thực hiện thanh toán qua các phương thức khác nhau và nhận hóa đơn
  Để hoàn tất giao dịch mua bán bánh kẹo một cách chính xác và bảo mật

  # =========================================================================
  # KỊCH BẢN 1: THANH TOÁN THÀNH CÔNG QUA VÍ ĐIỆN TỬ HOẶC THẺ
  # =========================================================================
  Scenario Outline: Thanh toán trực tuyến thành công cho đơn hàng bánh kẹo
    Given đơn hàng bánh kẹo có tổng giá trị là <Amount> VNĐ
    When khách hàng chọn phương thức thanh toán là "<PaymentMethod>" và xác thực giao dịch
    Then kết quả giao dịch thanh toán trả về là "Thành công"
    And đơn hàng được lưu ở trạng thái "PAID" và audit được ghi nhận
    And hệ thống tiến hành kích hoạt tính năng "<InvoiceAction>"
    And quy tắc bảo mật giao dịch tài chính [BR31] được áp dụng "<SecurityRule>"
    And cổng thanh toán được gọi "Có"

    Examples:
      | Amount | PaymentMethod | InvoiceAction       | SecurityRule           |
      | 240000 | Ví MoMo       | Gửi hóa đơn điện tử | Mã hóa token giao dịch |
      | 500000 | Thẻ Ngân hàng | Gửi hóa đơn điện tử | Xác thực 3D Secure     |

  # =========================================================================
  # KỊCH BẢN 2: THANH TOÁN KHI NHẬN HÀNG (COD)
  # =========================================================================
  Scenario Outline: Xử lý thanh toán tiền mặt khi nhận hàng (COD)
    Given đơn hàng bánh kẹo có tổng giá trị là <Amount> VNĐ
    When khách hàng chọn hình thức thanh toán là "Tiền mặt (COD)" khi giao hàng
    Then trạng thái giao dịch tài chính ban đầu là "<TransactionState>"
    And đơn hàng được lưu ở trạng thái "PENDING" và audit được ghi nhận
    And hệ thống tiến hành kích hoạt tính năng "<InvoiceAction>"
    And quy tắc bảo mật giao dịch tài chính [BR31] được áp dụng "<SecurityRule>"
    And cổng thanh toán được gọi "Không"

    Examples:
      | Amount | TransactionState  | InvoiceAction   | SecurityRule              |
      | 150000 | Đang chờ thu tiền | In hóa đơn giấy | Xác nhận biên lai tại chỗ |

  # =========================================================================
  # KỊCH BẢN 3: GIAO DỊCH TRỰC TUYẾN THẤT BẠI DO LỖI CỔNG
  # =========================================================================
  Scenario Outline: Xử lý giao dịch thanh toán trực tuyến bị lỗi
    Given khách hàng thực hiện thanh toán số tiền <Amount> VNĐ bằng phương thức "<PaymentMethod>"
    When cổng thanh toán phản hồi lỗi "<GatewayError>"
    Then kết quả giao dịch thanh toán trả về là "Thất bại"
    And hệ thống hiển thị thông báo "<ErrorMessage>" và yêu cầu khách hàng thử lại
    And đơn hàng được lưu ở trạng thái "PENDING" và audit được ghi nhận
    And quy tắc bảo mật giao dịch tài chính [BR31] được áp dụng "<SecurityRule>"
    And cổng thanh toán được gọi "Có"

    Examples:
      | Amount | PaymentMethod   | GatewayError                     | ErrorMessage                    | SecurityRule                 |
      | 320000 | Chuyển khoản QR | Lỗi kết nối ngân hàng / Hết giờ  | Lỗi kết nối ngân hàng / Hết giờ | Kiểm tra lỗi cổng thanh toán |
      | 320000 | Chuyển khoản QR | Giao dịch bị ngân hàng từ chối   | Giao dịch bị ngân hàng từ chối  | Kiểm tra lỗi cổng thanh toán |
