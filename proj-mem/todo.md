# BẢNG CÔNG VIỆC, QUYẾT ĐỊNH & VẤN ĐỀ TỒN ĐỌNG (TODO & TRACKING)

## 1. Việc Đang Làm (In Progress)
- [x] Khởi tạo tài liệu dự án và bộ nhớ ngữ cảnh (`proj-mem/` gồm 6 file).
- [x] Viết script khởi chạy nhanh một bước [run.bat](file:///f:/PKA/Code/DALN/run.bat) cho toàn bộ hệ thống.
- [x] **Bước 1 (AI Chatbot Database):** Đã tạo schema migration `BE/chat_schema.sql`, JPA Entities (`ChatSession`, `ChatMessage`, `ChatToolLog`) và Repositories.
- [x] **Bước 2 (AI Backend & Function Calling):** Đã xây dựng `AiToolService` (4 tools: `search_products`, `check_stock`, `get_low_stock_products`, `get_top_selling_products`), `OllamaClient`, `AiChatService` và `ChatController` (`POST /api/chat`, `GET /api/chat/sessions`, `GET /api/chat/sessions/{sessionId}/messages`). Đã kết nối thành công với Ollama local (model `qwen2.5:7b-instruct`), phân quyền linh hoạt theo JWT role (`ROLE_ADMIN` / `ROLE_USER` / Guest) để AI chủ động gọi tool quản trị khi có quyền và từ chối khi là khách.
- [x] **Bước 3 (AI Frontend):** Đã tạo component [ChatWidget.vue](file:///f:/PKA/Code/DALN/frontend/app/components/ChatWidget.vue), gắn vào layout toàn cục [default.vue](file:///f:/PKA/Code/DALN/frontend/app/layouts/default.vue), hỗ trợ câu hỏi gợi ý, tự động lưu session vào `sessionStorage`, tự động đính kèm JWT Bearer token và fallback sang khách nếu token hết hạn.


## 2. Lỗi & Bất Cập Đã Biết (Known Issues)
1. **Lệnh `pnpm` không khả dụng:**
   - *Mô tả:* Lệnh `pnpm` chưa được thiết lập vào biến môi trường PATH của hệ thống máy tính Windows hiện tại.
   - *Cách khắc phục hiện tại:* Sử dụng `npm` (`npm run dev`) để khởi chạy frontend.
2. **Docker Desktop không chạy tự động:**
   - *Mô tả:* Lệnh `docker ps` báo lỗi không kết nối được pipe của Docker Desktop Linux Engine.
   - *Cách khắc phục hiện tại:* Chạy Redis trực tiếp qua file thực thi nhị phân `redis-server.exe` có sẵn trong WinGet packages.
3. **Môi trường kết nối PostgreSQL:**
   - *Mô tả:* Cần bảo đảm dịch vụ PostgreSQL port `5432` luôn bật trước khi backend chạy để tránh ngắt kết nối datasource.

## 3. Quyết Định Kỹ Thuật Đã Thống Nhất (Technical Decisions)
| Ngày | Quyết định | Lý do / Bối cảnh |
|---|---|---|
| 2026-10-10 | Sử dụng `run.bat` mở 3 terminal tách biệt | Giúp dev quan sát log trực tiếp của cả Redis, Spring Boot và Nuxt mà không bị nghẽn log vào 1 cửa sổ chung. |
| 2026-10-10 | Lưu trữ Refresh Token trong HttpOnly Cookie | Bảo mật tuyệt đối trước các đòn tấn công XSS trên trình duyệt, không lưu token nhạy cảm trong `localStorage`. |
| 2026-10-10 | Sử dụng Redis cho Token Blacklist | Giúp kiểm tra tính hợp lệ của token nhanh chóng với độ trễ thấp (sub-millisecond) mỗi khi có request tới gateway. |
| 2026-10-10 | Tách biệt hoàn toàn DTO và Entity | Ngăn ngừa đệ quy serialize khi lazy loading, bảo vệ cấu trúc database ngầm bên dưới. |

## 4. Các Vấn Đề Chưa Giải Quyết & Kế Hoạch Tiếp Theo (Backlog & Roadmap)
1. **Tích hợp Trợ Lý AI (AI Chatbot Assistant):**
   - Lựa chọn SDK tích hợp: Spring AI với Gemini API hoặc OpenAI API.
   - Phân hệ AI Admin: Viết prompt template để AI chuyển đổi câu hỏi tự nhiên thành câu truy vấn báo cáo doanh thu / tồn kho.
   - Phân hệ AI Client: Tư vấn thông số kỹ thuật (vd: gợi ý IC thay thế, tính toán công suất điện trở).
2. **Tự Động Hóa Kiểm Thử (Testing Suite):**
   - Bổ sung unit tests cho các service quan trọng (`OrderService`, `AuthenticationService`).
   - Bổ sung component/integration test cho giỏ hàng và thanh toán trên frontend.
3. **Triển Khai & Containerization (Production Deployment):**
   - Viết `Dockerfile` đa tầng (multi-stage build) cho Backend (Maven + JRE 21) và Frontend (Node / Nginx).
   - Thiết lập CI/CD pipeline tự động build và test khi push code.
