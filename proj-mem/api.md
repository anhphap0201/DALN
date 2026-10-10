# TÀI LIỆU API & ĐẶC TẢ GIAO TIẾP (API SPECIFICATION)

## 1. Chuẩn Định Dạng Yêu Cầu & Phản Hồi (Request / Response Standard)
- **Base URL:** `http://localhost:8080` (hoặc `http://localhost:8080/api`)
- **Headers bắt buộc khi gọi API bảo vệ:**
  ```http
  Authorization: Bearer <access_token>
  Content-Type: application/json
  ```
- **Cấu trúc phản hồi lỗi chuẩn (Global Exception Response):**
  ```json
  {
    "error": "Tên lỗi hoặc mô tả chi tiết",
    "message": "Thông điệp phản hồi thân thiện với người dùng"
  }
  ```
- **Cấu trúc phản hồi lỗi Validation (400 Bad Request):**
  ```json
  {
    "fieldErrors": {
      "email": "Email không hợp lệ",
      "password": "Mật khẩu phải có ít nhất 6 ký tự"
    }
  }
  ```

---

## 2. Danh Sách Endpoint Chi Tiết

### 2.1. Xác Thực & Người Dùng (Authentication & User)
| Phương thức | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| `POST` | `/auth/register` | Public | Đăng ký tài khoản mới (`RegisterRequest` -> `RegisterResponse`) |
| `POST` | `/auth/login` | Public | Đăng nhập (`LoginRequest` -> `LoginResponse` + Cookie `refresh_token`) |
| `POST` | `/auth/refresh` | Public (Cookie) | Cấp mới access token bằng refresh token trong cookie |
| `POST` | `/auth/logout` | Authenticated | Đăng xuất, đưa token vào Blacklist |
| `GET` | `/auth/verify-email?token={token}` | Public | Kích hoạt tài khoản qua link email |
| `POST` | `/auth/forgot-password` | Public | Gửi mail đặt lại mật khẩu |
| `POST` | `/auth/reset-password` | Public | Nhập token và mật khẩu mới |
| `GET` | `/api/user/profile` | USER / ADMIN | Lấy thông tin tài khoản hiện tại |
| `PUT` | `/api/user/profile` | USER / ADMIN | Cập nhật thông tin cá nhân |

### 2.2. Sản Phẩm & Biến Thể (Products & Variants)
| Phương thức | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| `GET` | `/api/products` | Public | Lấy toàn bộ danh sách sản phẩm |
| `GET` | `/api/products/{id}` | Public | Lấy thông tin chi tiết sản phẩm và các biến thể |
| `POST` | `/api/products` | ADMIN | Thêm sản phẩm mới kèm danh mục & thông số kỹ thuật |
| `PUT` | `/api/products/{id}` | ADMIN | Cập nhật thông tin chi tiết sản phẩm |
| `DELETE` | `/api/products/{id}` | ADMIN | Xóa sản phẩm |
| `GET` | `/api/search?keyword={kw}&category={cat}&...` | Public | Tìm kiếm & lọc linh kiện theo thông số |

### 2.3. Danh Mục (Categories)
| Phương thức | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| `GET` | `/api/categories` | Public | Lấy cây danh mục sản phẩm |
| `POST` | `/api/categories` | ADMIN | Tạo mới danh mục |
| `PUT` | `/api/categories/{id}` | ADMIN | Sửa danh mục |
| `DELETE` | `/api/categories/{id}` | ADMIN | Xóa danh mục |

### 2.4. Giỏ Hàng (Cart)
| Phương thức | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| `GET` | `/api/cart` | USER | Xem giỏ hàng của người dùng hiện tại |
| `POST` | `/api/cart/items` | USER | Thêm biến thể sản phẩm vào giỏ (`variantId`, `quantity`) |
| `PUT` | `/api/cart/items/{id}` | USER | Cập nhật số lượng mặt hàng trong giỏ |
| `DELETE` | `/api/cart/items/{id}` | USER | Xóa mặt hàng khỏi giỏ |

### 2.5. Đơn Hàng & Thanh Toán (Orders & Payments)
| Phương thức | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| `POST` | `/api/orders` | USER / Public | Tạo đơn hàng mới (Hỗ trợ cả Guest Checkout) |
| `GET` | `/api/orders` | USER | Lấy danh sách lịch sử đơn hàng của người dùng |
| `GET` | `/api/orders/{id}` | USER / ADMIN | Xem chi tiết 1 đơn hàng |
| `POST` | `/api/payment/momo/create` | USER / Public | Khởi tạo liên kết / QR thanh toán qua MoMo |
| `POST` | `/api/momo/ipn` | Public | Webhook nhận thông báo kết quả giao dịch từ server MoMo |
| `GET` | `/api/momo/return` | Public | Redirect URL sau khi người dùng thực hiện thanh toán trên MoMo |

### 2.6. Smart Locker & Tiện Ích
| Phương thức | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| `GET` | `/api/lockers` | Public | Lấy danh sách điểm đặt tủ thông minh và ngăn trống |
| `POST` | `/api/webhook/delivery` | Public | Webhook nhận cập nhật trạng thái từ hệ thống tủ |
| `POST` | `/api/upload` | USER / ADMIN | Tải lên hình ảnh sản phẩm/avatar (Multipart File) |

### 2.7. Trợ Lý AI Chatbot (AI Assistant & Stock Q&A)
| Phương thức | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| `POST` | `/api/chat` | Public / USER / ADMIN | Gửi câu hỏi cho AI Chatbot; tự động gọi Function Calling (`search_products`, `check_stock`, `get_low_stock_products`) |
| `GET` | `/api/chat/sessions` | USER / ADMIN | Lấy danh sách các phiên chat của tài khoản hiện tại |
| `GET` | `/api/chat/sessions/{sessionId}/messages` | Public / USER | Lấy toàn bộ lịch sử tin nhắn trong phiên chat |

---


## 3. Các Mã Trạng Thái HTTP Chuẩn (Status Codes)
- **`200 OK`**: Truy vấn hoặc cập nhật thành công.
- **`201 Created`**: Tạo mới tài nguyên thành công (Đăng ký, tạo sản phẩm, đặt đơn).
- **`400 Bad Request`**: Dữ liệu gửi lên sai định dạng, thiếu trường hoặc vi phạm validation.
- **`401 Unauthorized`**: Chưa cung cấp token, token hết hạn hoặc sai thông tin đăng nhập.
- **`403 Forbidden`**: Token hợp lệ nhưng không đủ quyền hạn (vd: User thường gọi endpoint Admin).
- **`404 Not Found`**: Không tìm thấy tài nguyên (sản phẩm, đơn hàng, người dùng).
- **`500 Internal Server Error`**: Lỗi nội bộ hệ thống hoặc xử lý ngoại lệ chưa đón đầu.
