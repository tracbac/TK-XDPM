Feature: Quản lý yêu cầu hủy đơn hàng bánh kẹo
  Như một khách hàng đã đặt bánh kẹo
  Tôi muốn hủy đơn trước khi đơn được giao cho đơn vị vận chuyển
  Để nhận lại số lượng tồn kho chính xác và bảo đảm quy tắc xử lý đơn

  # =========================================================================
  # KỊCH BẢN 1: HỦY ĐƠN THÀNH CÔNG VÀ HOÀN TRẢ TỒN KHO
  # =========================================================================
  Scenario Outline: Hủy đơn chưa được giao cho đơn vị vận chuyển
    Given khách hàng chọn đơn hàng bánh kẹo mã "<OrderId>" hiện đang ở trạng thái "<InitialStatus>"
    When hệ thống nhận yêu cầu hủy đơn với lý do là "<Reason>"
    Then kết quả xử lý trả về phải là "Thành công"
    And trạng thái mới của đơn hàng trong hệ thống sẽ là "CANCELLED"
    And quy tắc hoàn trả tồn kho bánh kẹo [BR17] được kích hoạt "Hoàn trả kho"
    And hệ thống ghi nhận nhật ký thao tác hủy đơn [BR12]
    And thông báo xử lý hủy đơn là "Không có lỗi"

    Examples:
      | OrderId | InitialStatus | Reason                   |
      | ORD-101 | PENDING       | Khách muốn đổi ý         |
      | ORD-104 | PROCESSING    | Thay đổi loại bánh đặt   |

  # =========================================================================
  # KỊCH BẢN 2: TỪ CHỐI HỦY ĐƠN ĐÃ GIAO ĐƠN VỊ VẬN CHUYỂN HOẶC ĐÃ HỦY
  # =========================================================================
  Scenario Outline: Không cho phép hủy đơn không còn đủ điều kiện
    Given khách hàng chọn đơn hàng bánh kẹo mã "<OrderId>" hiện đang ở trạng thái "<InitialStatus>"
    When hệ thống nhận yêu cầu hủy đơn với lý do là "<Reason>"
    Then kết quả xử lý trả về phải là "<ExpectedResult>"
    And trạng thái mới của đơn hàng trong hệ thống sẽ là "<ExpectedStatus>"
    And quy tắc hoàn trả tồn kho bánh kẹo [BR17] được kích hoạt "Giữ nguyên kho"
    And hệ thống ghi nhận nhật ký thao tác hủy đơn [BR12]
    And thông báo xử lý hủy đơn là "<ErrorMessage>"

    Examples:
      | OrderId | InitialStatus | Reason                        | ExpectedResult | ExpectedStatus | ErrorMessage                                                        |
      | ORD-103 | SHIPPED       | Đơn đang giao không muốn nhận | Từ chối        | SHIPPED        | [BR11] Không thể hủy đơn hàng khi đang giao hoặc đã giao thành công. |
      | ORD-105 | DELIVERED     | Đơn đã giao thành công        | Từ chối        | DELIVERED      | [BR11] Không thể hủy đơn hàng khi đang giao hoặc đã giao thành công. |
      | ORD-102 | CANCELLED     | Yêu cầu hủy lại đơn cũ        | Thất bại       | CANCELLED      | Đơn hàng đã được hủy trước đó.                                      |

  # =========================================================================
  # KỊCH BẢN 3: KHÔNG TÌM THẤY ĐƠN HÀNG
  # =========================================================================
  Scenario: Từ chối yêu cầu hủy với mã đơn không tồn tại
    Given khách hàng chọn đơn hàng bánh kẹo mã "ORD-999" hiện đang ở trạng thái "NOT_FOUND"
    When hệ thống nhận yêu cầu hủy đơn với lý do là "Mã đơn hàng không có thực"
    Then kết quả xử lý trả về phải là "Thất bại"
    And trạng thái mới của đơn hàng trong hệ thống sẽ là "NOT_FOUND"
    And quy tắc hoàn trả tồn kho bánh kẹo [BR17] được kích hoạt "Giữ nguyên kho"
    And hệ thống ghi nhận nhật ký thao tác hủy đơn [BR12]
    And thông báo xử lý hủy đơn là "Không tìm thấy đơn hàng: ORD-999"
