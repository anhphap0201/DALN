# PROJECT MEMORY: E-Commerce Electronic Components

## 1. Tổng quan
- **Mục tiêu:** Website bán linh kiện điện tử tích hợp trợ lý AI (tư vấn kỹ thuật, báo cáo kinh doanh).
- **Mô hình:** B2C, lọc chi tiết thông số linh kiện, hỗ trợ Guest Checkout, Smart Locker, thanh toán MoMo.

## 2. Kiến trúc & Công nghệ
- **Backend:** 
  - Thư mục: `BE/E-commerce-Electronic-Components-Backend`
  - Ngôn ngữ & Framework: Java 21, Spring Boot 4.0.1, Spring Security + JWT, Spring Data JPA.
  - Build tool: Maven (`mvnw.cmd` / `mvnw`).
- **Frontend:**
  - Thư mục: `frontend`
  - Ngôn ngữ & Framework: Nuxt 4.2.2, Vue 3.5, TypeScript, Tailwind CSS 4.
  - Package manager: npm (`pnpm` hiện không có trong PATH hệ thống).
- **Database & Cache:**
  - PostgreSQL 15: port `5432`, database `test_api` (user/pass: `postgres` / `123321`).
  - Redis 8.x: port `6379` (chạy qua binary `redis-server.exe` trên Windows).

## 3. Cổng & Đường dẫn kiểm tra (Endpoints)
- **Frontend:** `http://localhost:3000`
- **Backend REST API:** `http://localhost:8080/api`
- **Swagger Documentation:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI Spec:** `http://localhost:8080/v3/api-docs`
- **Redis:** `localhost:6379`

## 4. Khởi động hệ thống
- **Tập lệnh gộp:** File `run.bat` tại thư mục gốc chạy cả 3 service trong các terminal riêng.
- **Tập lệnh riêng lẻ:**
  - `run-redis.bat`: Khởi động Redis server (Port 6379).
  - `run-be.bat`: Khởi động Backend Spring Boot (Port 8080).
  - `run-fe.bat`: Khởi động Frontend Nuxt (Port 3000).
- **Lệnh thủ công:**
  - Redis: `redis-server`
  - Backend: `cd BE\E-commerce-Electronic-Components-Backend && .\mvnw spring-boot:run`
  - Frontend: `cd frontend && npm run dev`

## 5. Lưu ý kỹ thuật & Gotchas
- `docker-compose.yaml` có sẵn trong thư mục backend cho Postgres và Redis nếu Docker daemon hoạt động.
- Máy hiện dùng trực tiếp `redis-server.exe` cài qua WinGet.
- Lệnh gọi FE dùng `npm run dev` thay vì `pnpm dev` do biến môi trường hiện tại chưa nhận `pnpm`.
