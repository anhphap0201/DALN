# THIẾT KẾ BACKEND (BACKEND ARCHITECTURE & COMPONENTS)

## 1. Cấu Trúc Tổng Thể & Tech Stack
- **Framework:** Spring Boot 4.0.1 trên nền Java 21 LTS.
- **Mô hình kiến trúc:** Mô hình phân lớp Controller - Service - Repository (CSR / Layered Architecture).
- **Thư mục gốc:** `BE/E-commerce-Electronic-Components-Backend/src/main/java/com/example/ecommerceelectroniccomponentsbackend`

```
├── config/             # Cấu hình Spring Security, JWT, CORS, Redis, MoMo, OpenAPI
├── controller/         # REST API Controllers (nhận HTTP request và trả ResponseEntity)
├── dto/                # Data Transfer Objects (Request & Response DTOs)
│   ├── request/
│   └── response/
├── entity/             # JPA Entities ánh xạ tới bảng cơ sở dữ liệu PostgreSQL
├── exception/          # Xử lý ngoại lệ tập trung (GlobalExceptionHandler)
├── mapper/             # Mapper chuyển đổi giữa Entity và DTO
├── model/              # Các model hỗ trợ xác thực & token (User, Role, Blacklist,...)
├── repository/         # Spring Data JPA Repositories
└── service/            # Interface và Implementation xử lý logic nghiệp vụ
```

## 2. Hệ Thống Thực Thể Cốt Lõi (Core JPA Entities)
| Entity | Mô tả nhiệm vụ | Bảng liên kết chính |
|---|---|---|
| `User` | Tài khoản người dùng hệ thống | `Role`, `Cart`, `Order`, `Review` |
| `Role` | Vai trò và quyền hạn (`ROLE_USER`, `ROLE_ADMIN`) | `User` |
| `Product` | Thông tin cơ bản linh kiện (tên, mô tả, hình ảnh, thông số kỹ thuật) | `Category`, `ProductVariant`, `Review` |
| `ProductVariant`| Biến thể linh kiện (giá, SKU, thông số riêng như gói chân cắm DIP/SMD, dải điện áp) | `Product`, `Inventory` |
| `Category` | Danh mục linh kiện theo cây phân cấp | `Product` |
| `Inventory` | Quản lý số lượng tồn kho theo từng biến thể sản phẩm | `ProductVariant` |
| `Cart` & `CartItem` | Giỏ hàng của người dùng lưu trữ các biến thể đã chọn | `User`, `ProductVariant` |
| `Order` & `OrderItem` | Đơn hàng, địa chỉ giao hàng, tổng tiền và trạng thái | `User`, `ProductVariant`, `Payment` |
| `Payment` | Giao dịch thanh toán (COD hoặc MoMo) | `Order` |
| `Review` | Đánh giá và bình luận sản phẩm của khách hàng | `Product`, `User` |
| `ChatSession` | Phiên hội thoại AI Chatbot (Guest/User) | `User` |
| `ChatMessage` | Lịch sử tin nhắn theo phiên (user, assistant, system) | `ChatSession` |
| `ChatToolLog` | Nhật ký gọi Tool Function Calling phục vụ Audit | `ChatSession`, `User` |

## 3. Các Dịch Vụ Nghiệp Vụ Chính (Services)
- **`IAuthenticationService` & `IUserService`:** Đăng ký, đăng nhập, cấp phát JWT, xác minh email qua token, quên mật khẩu, thu hồi token.
- **`IProductService` & `ProductSearchService`:** Truy vấn sản phẩm, tìm kiếm từ khóa, quản lý biến thể và lọc thông số kỹ thuật.
- **`ICartService`:** Thêm, cập nhật số lượng, xóa sản phẩm khỏi giỏ hàng.
- **`IOrderService`:** Tạo đơn hàng, tính tổng tiền, trừ tồn kho, cập nhật trạng thái đơn (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED).
- **`MomoService`:** Tạo liên kết thanh toán MoMo QR/Gateway và xử lý Webhook IPN từ MoMo.
- **`SmartLockerService`:** Tích hợp tra cứu tủ thông minh và webhook cập nhật trạng thái giao nhận.
- **`FileUploadService`:** Tiếp nhận tải lên hình ảnh sản phẩm/avatar và lưu trữ vào thư mục `/uploads`.

## 4. Các Cấu Hình Trọng Tâm (Configurations)
- **`SecurityConfig`:** Thiết lập filter chain, tắt CSRF cho REST API, khai báo Resource Server JWT Decoder, cấu hình CORS và phân quyền endpoint.
- **`RedisConfig`:** Kết nối Redis phục vụ lưu trữ blacklist token và caching dữ liệu truy vấn.
- **`OpenApiConfig`:** Tích hợp Swagger / SpringDoc OpenAPI 3 phục vụ test API trực quan tại `/swagger-ui/index.html`.
- **`MomoConfig` & `SmartLockerConfig`:** Đọc cấu hình partner code, secret key, access key từ biến môi trường hoặc file properties.

## 5. Cơ Sở Dữ Liệu & Dữ Liệu Mẫu (Database Schema & Seeds)
*Nguồn:* [BE/sql.sql](file:///f:/PKA/Code/DALN/BE/sql.sql) (PostgreSQL 15)

### Bảng Cơ Sở Dữ Liệu
1. `users`: Tài khoản, bcrypt hash, họ tên, sđt, địa chỉ (`province`, `ward`, `detailed_address`), `role` (`ROLE_USER` / `ROLE_ADMIN`), `is_active`.
2. `categories`: Danh mục linh kiện (`name`, `slug`, `image_url`, `description`).
3. `products`: Sản phẩm chính (`name`, `slug`, `avg_rating`, `sold_quantity`).
4. `product_categories`: Quan hệ nhiều-nhiều giữa `products` và `categories`.
5. `product_variants`: Biến thể SKU (`variant_name`, `description`, `specifications`, `price`, `discount_price`, `in_stock`, `is_available`).
6. `carts` & `cart_items`: Giỏ hàng theo `user_id` và các `product_variant_id` kèm `quantity`.
7. `orders` & `order_items`: Đơn hàng, `total_cost`, `shipping_cost`, `payment_method`, `status`, chi tiết từng biến thể và đơn giá.
8. `reviews`: Đánh giá 1-5 sao và comment (`UNIQUE (user_id, product_id)`).
9. `articles`: Bài viết hướng dẫn/tin tức (`author_id`, `title`, `slug`, `content`).
10. `notifications` & `user_notifications`: Thông báo hệ thống và trạng thái đọc `is_read`.

### Tài Khoản Mẫu Đăng Nhập Sẵn (Seed Accounts)
- **Admin:** `he@gmail.com` / `admin123` (Role: `ROLE_ADMIN`)
- **User 1:** `user@gmail.com` / `user123` (Role: `ROLE_USER`)
- **User 2:** `maria@gmail.com` / `user123` (Role: `ROLE_USER`)

### Dữ Liệu Mẫu Linh Kiện
- 8 Danh mục: Arduino & Modules, Raspberry Pi, Linh kiện điện tử, Cảm biến, Module nguồn, Module truyền thông, Màn hình hiển thị, Motor & Driver.
- 28 Sản phẩm & hàng chục biến thể với đầy đủ thông số kỹ thuật (`specifications`), giá, tồn kho sẵn sàng thử nghiệm.

