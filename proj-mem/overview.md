# TỔNG QUAN DỰ ÁN (PROJECT OVERVIEW)

## 1. Thông Tin & Mục Tiêu Dự Án
- **Tên dự án:** Website Bán Linh Kiện Điện Tử Tích Hợp Trợ Lý AI
- **Mô hình:** B2C (Business-to-Consumer)
- **Mục tiêu cốt lõi:**
  1. Cung cấp nền tảng thương mại điện tử chuyên biệt cho linh kiện và phụ kiện điện tử (IC, tụ, trở, vi điều khiển, cảm biến,...).
  2. Lọc đa chiều theo thông số kỹ thuật chi tiết (điện áp, dung lượng, công suất, chân cắm DIP/SMD).
  3. Tích hợp thanh toán trực tuyến (MoMo), giao nhận Smart Locker và mua hàng nhanh (Guest Checkout).
  4. Tích hợp Trợ lý AI: Tư vấn kỹ thuật lựa chọn linh kiện cho khách hàng và truy vấn dữ liệu kinh doanh/tồn kho cho quản trị viên.

## 2. Kiến Trúc Cấp Cao (High-Level Architecture)
- **Kiến trúc:** Client - Server tách rời (Decoupled Architecture), giao tiếp hoàn toàn qua RESTful API chuẩn JSON.
- **Frontend:** Nuxt 4 (Vue 3, TypeScript, Tailwind CSS) phục vụ giao diện người dùng và quản trị viên.
- **Backend:** Spring Boot 4 (Java 21) xử lý nghiệp vụ, xác thực JWT, phân quyền, kết nối cơ sở dữ liệu.
- **Cơ sở dữ liệu:** PostgreSQL 15 lưu trữ dữ liệu quan hệ (sản phẩm, đơn hàng, người dùng, tồn kho).
- **Caching & Session:** Redis 8.x lưu trữ blacklist token, refresh token và cache tạm thời.

## 3. Môi Trường Vận Hành & Endpoints
- **Frontend Port:** `3000` (`http://localhost:3000`)
- **Backend API Port:** `8080` (`http://localhost:8080/api`)
- **Swagger Documentation:** `http://localhost:8080/swagger-ui/index.html`
- **PostgreSQL Port:** `5432` (Database: `test_api`)
- **Redis Port:** `6379`
- **Khởi động nhanh:** Chạy file `run.bat` tại thư mục gốc để mở đồng thời Redis, BE và FE.

## 4. Quy Tắc Chung (General Engineering Rules)
1. **TypeScript Strict:** Mọi code frontend phải định nghĩa rõ kiểu, tránh ép kiểu `any` tùy tiện.
2. **Clean Architecture & Separation of Concerns:**
   - BE: Controller chỉ nhận/validate request và trả DTO; logic nằm trong Service; Repository chỉ đảm nhận truy vấn DB.
   - FE: Tách biệt UI (Components/Pages) và Business Logic/State (Composables).
3. **Lazy / Minimalist Development (Ponytail principle):**
   - Viết code tối giản giải quyết đúng yêu cầu, không thêm boilerplate/tính năng suy diễn chưa cần thiết.
   - Tận dụng tối đa thư viện hiện có, giữ nguyên convention và cấu trúc sẵn có của repo.
4. **Error Handling & Data Integrity:**
   - Tuyệt đối không nuốt lỗi; xử lý lỗi tập trung qua Global Exception Handler ở BE và Composables notification ở FE.
