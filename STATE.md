# 📋 BÁO CÁO TRẠNG THÁI HỆ THỐNG (STATE.MD)

> **Dự án:** ECShop - Cửa hàng Thương mại Điện tử Linh kiện Điện tử & Trợ lý Kỹ thuật AI  
> **Cập nhật lần cuối:** 2026-10-28 02:25 (Múi giờ: Asia/Ho_Chi_Minh)  
> **Trạng thái:** Toàn bộ hệ thống Backend và Frontend đang hoạt động ổn định. Đã vượt qua toàn bộ **53/53 bài kiểm thử tự động** Backend (`BUILD SUCCESS`) và hoàn thành đóng gói biên dịch sản xuất Frontend Nuxt 4 (`✨ Build complete!`, 0 lỗi).

---

## 1. Kiến trúc & Công nghệ

- **Backend:** 
  - Spring Boot 4.0.1, Java 25 (Java Target 17)
  - PostgreSQL 16 (Hibernate JPA `ddl-auto: update`)
  - Redis 7.x / 8.x
  - Cổng thanh toán trực tuyến: **PayOS (VietQR)** (Đã loại bỏ hoàn toàn VNPAY)
  - Quản lý giao nhận tự động: **Smart Locker IoT**
  - Ollama Native Tool Calling (`qwen2.5:7b-instruct` hoặc `qwen2.5:3b`)
- **Frontend:**
  - Nuxt 4.2.2 (Vue 3.5, Vite 7, Nitro 2.13)
  - Tailwind CSS 4, Lucide Icons, Pinia
- **Thời gian & Định dạng:** Chuẩn múi giờ Việt Nam `Asia/Ho_Chi_Minh`, tiền tệ VND.

---

## 2. Các Tính năng Mới & Cải tiến Vừa Hoàn Thành (Milestone Mới Nhất)

### 🛒 1. Mua hàng Không cần Đăng nhập (Guest Checkout Flow)
- **Mục tiêu:** Cho phép khách vãng lai chưa đăng nhập có thể chọn linh kiện, thêm vào giỏ hàng, cập nhật số lượng và tiến hành đặt hàng trực tiếp bằng cách điền thông tin người nhận và địa chỉ giao hàng.
- **Backend:**
  - [`CreateOrderRequest.java`](file:///f:/PKA/Code/DALN/BE/E-commerce-Electronic-Components-Backend/src/main/java/com/example/ecommerceelectroniccomponentsbackend/dto/CreateOrderRequest.java): Bổ sung các trường thông tin nhận hàng (`fullName`, `phone`, `email`, `address`, `province`, `ward`, `provinceCode`, `wardCode`) và danh sách sản phẩm `items: List<OrderItemRequest(productVariantId, quantity)>`.
  - [`OrderDTO.java`](file:///f:/PKA/Code/DALN/BE/E-commerce-Electronic-Components-Backend/src/main/java/com/example/ecommerceelectroniccomponentsbackend/dto/OrderDTO.java) & [`OrderMapper.java`](file:///f:/PKA/Code/DALN/BE/E-commerce-Electronic-Components-Backend/src/main/java/com/example/ecommerceelectroniccomponentsbackend/mapper/OrderMapper.java): Cho phép `userId` là `null`, ánh xạ đầy đủ thông tin người nhận và địa chỉ.
  - [`OrderController.java`](file:///f:/PKA/Code/DALN/BE/E-commerce-Electronic-Components-Backend/src/main/java/com/example/ecommerceelectroniccomponentsbackend/controller/OrderController.java) & [`PayOSController.java`](file:///f:/PKA/Code/DALN/BE/E-commerce-Electronic-Components-Backend/src/main/java/com/example/ecommerceelectroniccomponentsbackend/controller/PayOSController.java): Thêm helper `getCurrentUserIdOrNull()` cho phép khách chưa đăng nhập gửi request tạo đơn hàng.
  - [`OrderServiceImpl.java`](file:///f:/PKA/Code/DALN/BE/E-commerce-Electronic-Components-Backend/src/main/java/com/example/ecommerceelectroniccomponentsbackend/service/impl/OrderServiceImpl.java): Xử lý luồng đặt hàng cho khách vãng lai khi `userId == null` (tra cứu biến thể từ `productVariantRepository`, kiểm tra tồn kho, validate thông tin bắt buộc gồm họ tên, SĐT, địa chỉ chi tiết, tỉnh/thành, phường/xã, tạo `Order` với `userId = null`).
- **Frontend:**
  - [`useCart.ts`](file:///f:/PKA/Code/DALN/frontend/app/composables/useCart.ts): Bổ sung lưu trữ giỏ hàng khách qua `localStorage` (`ecshop_guest_cart`) khi chưa đăng nhập cho `fetchCart`, `addToCart`, `updateCartItem`, `removeFromCart`, `clearCart`.
  - [`layouts/default.vue`](file:///f:/PKA/Code/DALN/frontend/app/layouts/default.vue): Gọi `fetchCart()` ngay khi mount để hiển thị badge số lượng sản phẩm trên header cho cả khách vãng lai.
  - [`products/[id].vue`](file:///f:/PKA/Code/DALN/frontend/app/pages/products/[id].vue) & [`index.vue`](file:///f:/PKA/Code/DALN/frontend/app/pages/index.vue): Bỏ chặn redirect bắt buộc login khi nhấn "Thêm vào giỏ" hoặc "Mua ngay", truyền thông tin biến thể vào composable giỏ hàng.
  - [`cart.vue`](file:///f:/PKA/Code/DALN/frontend/app/pages/cart.vue): Bỏ `middleware: ['auth']`, cho phép khách xem và chỉnh sửa giỏ hàng, chỉ tải lịch sử mua lại khi đã đăng nhập.
  - [`checkout.vue`](file:///f:/PKA/Code/DALN/frontend/app/pages/checkout.vue):
    - Bỏ `middleware: ['auth']`.
    - Thay thế khung hiển thị địa chỉ tĩnh bằng form nhập liệu tương tác trực tiếp (`Họ và tên`, `Số điện thoại`, `Email`, `Tỉnh/Thành phố`, `Phường/Xã/Quận/Huyện`, `Địa chỉ cụ thể`, `Ghi chú`). Có banner thông báo khách vãng lai.
    - Khi đặt hàng thành công (COD), hiển thị Modal xác nhận đơn hàng thành công kèm mã đơn, thông tin nhận hàng và tổng tiền thay vì chuyển hướng vào trang `/orders` (vốn yêu cầu đăng nhập).

---

### 💳 2. Chuẩn hóa Cổng Thanh Toán (Chỉ giữ PayOS, Xóa bỏ hoàn toàn VNPAY)
- **Mục tiêu:** Tinh giản hạ tầng thanh toán, tập trung vào cổng thanh toán trực tuyến tự động **PayOS (VietQR)** và phương thức thanh toán tiền mặt khi nhận hàng (**COD**).
- **Backend:**
  - **Đã xóa các file VNPAY:**
    - `VnPayConfig.java`
    - `VnPayController.java`
    - `IVnPayService.java`
    - `VnPayServiceImpl.java`
  - [`SecurityConfig.java`](file:///f:/PKA/Code/DALN/BE/E-commerce-Electronic-Components-Backend/src/main/java/com/example/ecommerceelectroniccomponentsbackend/config/SecurityConfig.java): Xóa các route `/api/vnpay/**`.
  - [`OrderServiceImpl.java`](file:///f:/PKA/Code/DALN/BE/E-commerce-Electronic-Components-Backend/src/main/java/com/example/ecommerceelectroniccomponentsbackend/service/impl/OrderServiceImpl.java): Loại bỏ kiểm tra payment method `VNPAY`, chuẩn hóa logic xử lý trạng thái thanh toán và tự động hủy sau 24h đối với đơn hàng trực tuyến sang duy nhất `PAYOS`.
  - [`PayOSController.java`](file:///f:/PKA/Code/DALN/BE/E-commerce-Electronic-Components-Backend/src/main/java/com/example/ecommerceelectroniccomponentsbackend/controller/PayOSController.java): Hỗ trợ đầy đủ tạo thanh toán `POST /api/payos/create`, thanh toán lại `POST /api/payos/retry-payment/{orderId}` cho cả khách đăng nhập và khách vãng lai, xử lý Webhook `POST /api/payos/webhook`.
  - [`AdminAnalyticsServiceTest.java`](file:///f:/PKA/Code/DALN/BE/E-commerce-Electronic-Components-Backend/src/test/java/com/example/ecommerceelectroniccomponentsbackend/service/ai/AdminAnalyticsServiceTest.java): Cập nhật fixture test sang `PAYOS`.
- **Frontend:**
  - **Đã xóa file:** `frontend/app/pages/payment/vnpay-return.vue`.
  - [`checkout.vue`](file:///f:/PKA/Code/DALN/frontend/app/pages/checkout.vue): Xóa `vnpay` khỏi `paymentMethods` (chỉ còn **COD** và **PayOS**), xóa luồng gọi API `/api/vnpay/create`.
  - [`orders.vue`](file:///f:/PKA/Code/DALN/frontend/app/pages/orders.vue): Cập nhật hàm kiểm tra chờ thanh toán sang `PAYOS`, cập nhật nút thanh toán lại (`retryPayment`) trỏ duy nhất về `/api/payos/retry-payment/{orderId}`, đổi modal yêu cầu hoàn tiền về PayOS/Bank.

---

### 🏷️ 3. Phân hệ Quản Lý & Thiết Lập Giảm Giá Cho Admin (`/admin/discounts`)
- **Mục tiêu:** Cung cấp trang quản trị tập trung giúp Admin thiết lập giá khuyến mãi trực tiếp cho từng linh kiện / biến thể sản phẩm (sử dụng trường `discount_price` có sẵn trong CSDL).
- **Backend:**
  - [`IProductVariantService.java`](file:///f:/PKA/Code/DALN/BE/E-commerce-Electronic-Components-Backend/src/main/java/com/example/ecommerceelectroniccomponentsbackend/service/IProductVariantService.java) & [`ProductVariantServiceImpl.java`](file:///f:/PKA/Code/DALN/BE/E-commerce-Electronic-Components-Backend/src/main/java/com/example/ecommerceelectroniccomponentsbackend/service/impl/ProductVariantServiceImpl.java):
    - `updateDiscount(id, discountPrice)`: Kiểm tra nghiệp vụ nếu `discountPrice` nhập vào phải `> 0` và `< price`; nếu bằng 0 hoặc null thì tự động xóa khuyến mãi (về nguyên giá).
    - `updateBatchDiscount(variantIds, discountPercent)`: Thiết lập giảm giá hàng loạt theo % cho danh sách linh kiện được chọn.
  - [`ProductVariantController.java`](file:///f:/PKA/Code/DALN/BE/E-commerce-Electronic-Components-Backend/src/main/java/com/example/ecommerceelectroniccomponentsbackend/controller/ProductVariantController.java):
    - Thêm endpoint `PATCH /product-variants/{id}/discount`.
    - Thêm endpoint `POST /product-variants/batch-discount`.
- **Frontend:**
  - **Tạo mới trang [`admin/discounts.vue`](file:///f:/PKA/Code/DALN/frontend/app/pages/admin/discounts.vue):**
    - **Thẻ thống kê KPI:** Tổng số linh kiện, số linh kiện đang giảm giá, số linh kiện nguyên giá, mức giảm sâu nhất (%).
    - **Thanh thao tác hàng loạt (Bulk Actions):** Chọn nhiều linh kiện qua checkbox để áp dụng giảm X% hoặc hủy giảm giá hàng loạt.
    - **Bộ lọc & Tìm kiếm:** Lọc theo tab (Tất cả / Đang giảm giá / Nguyên giá), lọc theo danh mục, sắp xếp theo mức giảm hoặc giá, tìm kiếm nhanh theo tên linh kiện/sản phẩm.
    - **Bảng thiết lập nhanh (Inline Editing):** Cho phép nhập trực tiếp `% giảm` (tự động tính giá VND) hoặc nhập thẳng số tiền VND, hiển thị badge `-XX%`, nút "Lưu" và nút "Hủy giảm giá" về giá gốc.
  - [`layouts/admin.vue`](file:///f:/PKA/Code/DALN/frontend/app/layouts/admin.vue):
    - Thêm mục điều hướng **"Thiết Lập Giảm Giá"** (`/admin/discounts`) vào menu bên trái của Admin.

---

## 3. Danh mục 15 AI Tools hiện có

| STT | Tên Tool | Cấp độ Quyền | Cơ chế Kiểm soát (Policy) | UI Card Tương Tác |
|:---:|:---|:---:|:---|:---:|
| 1 | `search_products` | Public | Cho phép mọi người dùng | ✅ Product Grid |
| 2 | `recommend_products` | Public | Cho phép mọi người dùng | ✅ Product Grid |
| 3 | `compare_products` | Public | Cho phép mọi người dùng | Phản hồi văn bản |
| 4 | `check_stock` | Public | Cho phép mọi người dùng | ✅ Stock Details |
| 5 | `get_low_stock_products` | ADMIN_ONLY | Bắt buộc `ROLE_ADMIN` qua Policy | ✅ Low Stock Card |
| 6 | `get_top_selling_products` | Public | Cho phép mọi người dùng | ✅ Product Grid |
| 7 | `get_revenue_report` | ADMIN_ONLY | Bắt buộc `ROLE_ADMIN` qua Policy | ✅ Revenue Card |
| 8 | `track_order` | OWNER_OR_VERIFIED | Admin / Chủ sở hữu / Khớp SĐT | ✅ Order Tracking |
| 9 | `find_project_components` | Public | Cho phép mọi người dùng | ✅ BOM / Product Grid |
| 10 | `check_component_compatibility` | Public | Cho phép mọi người dùng | ✅ Compatibility Card |
| 11 | `build_bill_of_materials` | Public | Cho phép mọi người dùng | ✅ Interactive BOM Card |
| 12 | `get_order_analytics` | ADMIN_ONLY | Bắt buộc `ROLE_ADMIN` qua Policy | ✅ Order Analytics Card |
| 13 | `get_inventory_report` | ADMIN_ONLY | Bắt buộc `ROLE_ADMIN` qua Policy | ✅ Inventory Value Card |
| 14 | `get_product_performance` | ADMIN_ONLY | Bắt buộc `ROLE_ADMIN` qua Policy | ✅ Performance Card |
| 15 | `get_customer_analytics` | ADMIN_ONLY | Bắt buộc `ROLE_ADMIN` qua Policy | ✅ Customer Retention Card |

---

## 4. Kết quả Kiểm thử & Xác minh (Verification)

- **Backend Test Suite (Maven Surefire):**  
  Chạy lệnh: `.\mvnw.cmd test`
  ```text
  [INFO] Running AdminAnalyticsServiceTest:        4/4 passed
  [INFO] Running AiChatServiceTest:                3/3 passed
  [INFO] Running AiSecuritySanitizerTest:          3/3 passed
  [INFO] Running AiToolPolicyServiceTest:          8/8 passed
  [INFO] Running AiToolServiceTest:                7/7 passed
  [INFO] Running ChatIntentAnalyzerTest:           8/8 passed
  [INFO] Running ChatSessionMemoryServiceTest:      2/2 passed
  [INFO] Running ProjectPlanningServiceTest:      4/4 passed
  [INFO] Running SearchHistoryServiceTest:         4/4 passed
  [INFO] Running BackendApplicationTests:          1/1 passed
  [INFO] Running ChatControllerIntegrationTest:    9/9 passed
  [INFO] Running PayOSServiceTest:                 3/3 passed
  [INFO] ---------------------------------------------------
  [INFO] Tests run: 53, Failures: 0, Errors: 0, Skipped: 0
  [INFO] BUILD SUCCESS
  ```
- **Frontend Production Build (Nuxt 4 / Vite / Nitro):**  
  Chạy lệnh: `npm run build`
  ```text
  √ Client built in 5852ms
  √ Server built in 3530ms
  [nitro] √ Generated public .output/public
  [nitro] i Building Nuxt Nitro server
  Σ Total size: 2.7 MB (655 kB gzip)
  — ✨ Build complete! (0 errors)
  ```

---

## 5. Hướng dẫn Khởi chạy & Vận hành

### Khởi động nhanh All-in-one
Chạy file batch tại thư mục gốc:
```cmd
run.bat
```
Khởi động đồng thời 4 dịch vụ:
1. `run-redis.bat`: Redis Server (cổng 6379).
2. `run-ollama.bat`: Ollama Model `qwen2.5:7b-instruct`.
3. `run-be.bat`: Spring Boot Backend (cổng 8080).
4. `run-fe.bat`: Nuxt 4 Frontend (cổng 3000).

---

## 6. Danh sách Tệp tin Chính Đã Thay Đổi trong Phiên Làm Việc

1. **Backend (`BE/E-commerce-Electronic-Components-Backend`):**
   - `config/SecurityConfig.java`: Gỡ bỏ route VNPAY.
   - `config/VnPayConfig.java`: *(Đã xóa)*.
   - `controller/VnPayController.java`: *(Đã xóa)*.
   - `service/IVnPayService.java` & `impl/VnPayServiceImpl.java`: *(Đã xóa)*.
   - `controller/OrderController.java`: Hỗ trợ `getCurrentUserIdOrNull()` cho khách vãng lai.
   - `controller/PayOSController.java`: Hỗ trợ tạo & thanh toán lại PayOS cho cả User và Guest.
   - `controller/ProductVariantController.java`: Bổ sung endpoint `PATCH /{id}/discount` và `POST /batch-discount`.
   - `service/IProductVariantService.java` & `impl/ProductVariantServiceImpl.java`: Nghiệp vụ cập nhật giảm giá đơn lẻ và hàng loạt.
   - `service/impl/OrderServiceImpl.java`: Logic Guest Checkout và chuyển đổi thanh toán trực tuyến sang duy nhất PayOS.
   - `dto/CreateOrderRequest.java` & `dto/OrderDTO.java` & `mapper/OrderMapper.java`: Ánh xạ thông tin giao nhận khách vãng lai.

2. **Frontend (`frontend/app`):**
   - `composables/useCart.ts`: Quản lý giỏ hàng `localStorage` cho khách vãng lai.
   - `layouts/admin.vue`: Menu điều hướng mới **"Thiết Lập Giảm Giá"** (`/admin/discounts`).
   - `layouts/default.vue`: Tự động nạp giỏ hàng khách khi mount.
   - `pages/cart.vue`: Bỏ `middleware: ['auth']`, hỗ trợ xem và sửa giỏ hàng khách.
   - `pages/checkout.vue`: Bỏ `middleware: ['auth']`, form nhập địa chỉ người nhận tương tác trực tiếp, modal xác nhận đơn khách, chỉ giữ thanh toán PayOS và COD.
   - `pages/orders.vue`: Cập nhật trạng thái chờ thanh toán PayOS và nút thanh toán lại PayOS.
   - `pages/payment/vnpay-return.vue`: *(Đã xóa)*.
   - `pages/products/[id].vue` & `pages/index.vue`: Cho phép khách thêm giỏ hàng & mua ngay.
   - `pages/admin/discounts.vue`: *(Tạo mới)* Trang quản trị & thiết lập giảm giá linh kiện tập trung.
