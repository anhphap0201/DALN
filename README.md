# ĐỀ XUẤT DỰ ÁN: XÂY DỰNG WEBSITE BÁN LINH KIỆN ĐIỆN TỬ TÍCH HỢP TRỢ LÝ AI

---

## 1. THÔNG TIN DỰ ÁN

* **Tên dự án:** Xây dựng Website bán linh kiện điện tử tích hợp Trợ lý AI
* **Lĩnh vực:** Thương mại điện tử (E-commerce) & Ứng dụng Trí tuệ nhân tạo (AI)
* **Mô hình:** B2C (Business-to-Consumer)
* **Đối tượng sử dụng:** Sinh viên, kỹ sư, người học kỹ thuật, khách hàng mua linh kiện và Quản trị viên (Admin).

---

## 2. GIỚI THIỆU DỰ ÁN

Dự án **“Xây dựng Website bán linh kiện điện tử tích hợp Trợ lý AI”** nhằm xây dựng một hệ thống thương mại điện tử chuyên cung cấp các sản phẩm linh kiện điện tử và cơ điện tử.

Khác với các website bán hàng thông thường, linh kiện điện tử thường có nhiều thông số kỹ thuật đặc thù (*điện áp, điện dung, công suất, sai số, số chân IC,...*). Người mua cần tìm kiếm chính xác thông số, trong khi Admin cần nắm bắt nhanh số liệu kinh doanh. Vì vậy, dự án tập trung vào 2 điểm đột phá:
1. **Tìm kiếm và lọc sản phẩm theo thông số kỹ thuật chi tiết.**
2. **Trợ lý AI thông minh (AI Bot Assistant):** Hỗ trợ Admin truy vấn doanh thu, tồn kho, sản phẩm bán chạy bằng ngôn ngữ tự nhiên; đồng thời tư vấn kỹ thuật và tra cứu cho khách hàng.

---

## 3. MỤC TIÊU DỰ ÁN

* Xây dựng website bán linh kiện điện tử giao diện trực quan, hiện đại, tối ưu UX/UI.
* Cho phép khách hàng tìm kiếm và lọc sản phẩm theo thông số kỹ thuật đặc thù.
* Cho phép xem chi tiết sản phẩm, lựa chọn biến thể/SKU, quản lý giỏ hàng và thanh toán.
* Hỗ trợ đặt hàng không cần đăng nhập (**Guest Checkout**) và theo dõi trạng thái đơn hàng.
* Xây dựng trang quản trị (Admin Dashboard) quản lý sản phẩm, danh mục, SKU, đơn hàng và tồn kho.
* **Tích hợp Trợ lý AI (AI Chatbot) nâng cao chuẩn Đồ án:**
  * **Dành cho Admin:** Hỏi đáp doanh thu, thống kê sản phẩm bán chạy, phát hiện tồn kho cảnh báo bằng giọng nói/văn bản.
  * **Dành cho Khách hàng:** Tư vấn lựa chọn linh kiện theo yêu cầu bài toán kỹ thuật, tra cứu thông số và đơn hàng.
* Áp dụng xác thực, phân quyền (**Spring Security & JWT**) và mã hóa dữ liệu an toàn.

---

## 4. PHẠM VI VÀ CHỨC NĂNG CHÍNH

### 4.1. Chức năng dành cho khách hàng

* **Xem và tìm kiếm sản phẩm:** Xem danh sách theo danh mục, tìm kiếm theo tên/mã sản phẩm.
* **Lọc sản phẩm theo thông số kỹ thuật (Chức năng nổi bật):**
  * *Điện trở:* Công suất, sai số, giá trị ôm.
  * *Tụ điện:* Điện dung, điện áp chịu đựng, loại tụ.
  * *IC:* Số chân, loại IC, kiểu đóng gói (DIP/SMD).
* **Xem chi tiết sản phẩm:** Hình ảnh, giá bán, mô tả, thông số kỹ thuật chi tiết, SKU và tình trạng tồn kho.
* **Giỏ hàng & Đặt hàng:** Quản lý giỏ hàng, hỗ trợ **Guest Checkout** (không bắt buộc đăng nhập).
* **Thanh toán:** Thanh toán khi nhận hàng (**COD**) và mô phỏng thanh toán online qua **MoMo**.
* **Theo dõi đơn hàng:** Trò chuyện hoặc tra cứu mã đơn hàng để xem trạng thái xử lý/vận chuyển.

### 4.2. Chức năng dành cho Admin

* Quản lý danh mục, thương hiệu, sản phẩm và biến thể SKU duy nhất.
* Quản lý tồn kho: Cập nhật số lượng, cảnh báo sản phẩm dưới ngưỡng an toàn.
* Quản lý đơn hàng: Duyệt đơn, cập nhật trạng thái đơn (Chờ xử lý, Đang giao, Đã hoàn thành, Hủy).
* Báo cáo thống kê: Biểu đồ doanh thu, số lượng đơn hàng, top sản phẩm bán chạy.
* Quản lý tài khoản và phân quyền người dùng (`ROLE_USER`, `ROLE_ADMIN`).

### 4.3. Chức năng Trợ lý AI (AI Smart Bot - Chức năng nâng cao)

Đây là tính năng điểm nhấn giúp nâng cao hàm lượng công nghệ cho đồ án:

* **Đối với Admin (AI Business Intelligence Bot):**
  * **Hỏi đáp doanh thu:** *"Doanh thu hôm nay/tháng này là bao nhiêu?"*, *"So sánh doanh thu tuần này với tuần trước"*.
  * **Thống kê sản phẩm:** *"Sản phẩm nào bán chạy nhất trong tháng 9?"*, *"Tụ điện nào có doanh số cao nhất?"*.
  * **Cảnh báo tồn kho:** *"Những linh kiện nào sắp hết hàng (số lượng < 10)?"*, *"Cần nhập thêm IC nào?"*.
  * **Tổng hợp báo cáo nhanh:** Nhận câu trả lời tổng hợp dưới dạng văn bản và bảng dữ liệu ngắn gọn.

* **Đối với Khách hàng (AI Technical Advisor Bot):**
  * **Tư vấn kỹ thuật:** *"Tôi cần làm mạch hạ áp từ 12V xuống 5V thì cần dùng IC và điện trở nào?"*, *"Tụ 16V có dùng được cho nguồn 24V không?"*.
  * **Gợi ý linh kiện:** Gợi ý danh sách sản phẩm có trong cửa hàng kèm đường link xem nhanh.
  * **Tra cứu đơn hàng:** *"Kiểm tra giúp tôi đơn hàng mã #ORD-88219"*.

---

## 5. CÔNG NGHỆ VÀ KIẾN TRÚC HỆ THỐNG

### 5.1. Công nghệ sử dụng

#### **Frontend**
* **Nuxt 3:** Framework Vue 3 xây dựng UI/UX hiện đại (Storefront, Filter System, Cart, Admin Dashboard, AI Chat Widget).
* **Tailwind CSS:** Thiết kế giao diện responsive, các thành phần UI (card, form, filter table, chat drawer).
* **Pinia:** Quản lý State phía Frontend (User Auth, Cart, Chat History, Tokens).

#### **Backend & AI Engine**
* **Java Spring Boot 4:** Xây dựng RESTful API, phân tầng kiến trúc và xử lý nghiệp vụ e-commerce.
* **Spring Security & JWT:** Xác thực bằng Access Token & Refresh Token, mã hóa password bằng `BCrypt`.
* **Spring AI / OpenAI API / Gemini API (Hoặc Ollama - Local LLM):** 
  * Tích hợp Đại mô hình ngôn ngữ (LLM) để xử lý ngôn ngữ tự nhiên (NLP).
  * Sử dụng kỹ thuật **Function Calling / Tool Calling** để cho phép AI tự động truy vấn CSDL PostgreSQL an toàn (lấy doanh thu, danh sách tồn kho, thông tin đơn hàng) và trả về phản hồi chính xác.
* **Hibernate / JPA:** Thao tác CSDL thông qua ORM Entity.

#### **Cơ sở dữ liệu & Công cụ**
* **Database:** PostgreSQL.
* **API Documentation & Versioning:** Swagger / OpenAPI, Git / GitHub.
* **Thiết kế & Quản lý:** Figma, Trello.

---

## 6. KIẾN TRÚC HỆ THỐNG TÍCH HỢP AI

Hệ thống áp dụng **Layered Architecture** mở rộng thêm phân hệ **AI Engine Service**:

```
                              ┌─────────────────────────────────────────┐
                              │           AI Engine Service             │
                              │  (Function Calling / Prompt / LLM API)  │
                              └────────────────────┬────────────────────┘
                                                   │ (Tool Call Queries)
                                                   ▼
Client (Nuxt 3 FE) ──> Controller ──> Service Layer ──> Repository ──> PostgreSQL DB
 (Storefront / Admin)   (REST API)    (Business / AI)  (JPA / Hibernate)
```

1. Client gửi câu hỏi ngôn ngữ tự nhiên (*"Doanh thu hôm nay bao nhiêu?"*) tới Backend AI Controller.
2. AI Service chuyển đổi ngữ cảnh, kích hoạt **Function Calling (Tool Calling)** gọi hàm `getRevenueByDateRange()`.
3. Service truy vấn PostgreSQL thông qua Repository để lấy số liệu thực tế.
4. LLM tổng hợp kết quả số liệu từ DB và phản hồi câu trả lời tự nhiên, chính xác cho client.

---

## 7. BẢO MẬT VÀ YÊU CẦU QUAN TRỌNG

### Bảo mật & Quyền riêng tư AI
* Xác thực **JWT**, mã hóa **BCrypt**, phân quyền nghiêm ngặt `ROLE_USER` và `ROLE_ADMIN`.
* **Bảo mật truy vấn AI:** Chỉ tài khoản `ROLE_ADMIN` mới có quyền sử dụng AI để truy vấn thông tin doanh thu, báo cáo tài chính và dữ liệu tồn kho nhạy cảm. Khách hàng thông thường chỉ dùng AI tư vấn kỹ thuật và tra cứu sản phẩm/đơn hàng cá nhân.
* Kiểm tra dữ liệu đầu vào (Input Validation), chống SQL Injection và Prompt Injection.

### Yêu cầu nghiệp vụ
* Mã SKU duy nhất cho từng biến thể linh kiện.
* Kiểm tra và giữ hàng tồn kho chính xác khi đặt hàng.
* AI phản hồi nhanh, chính xác số liệu từ CSDL, không "bịa" (hallucinate) thông tin doanh thu hay tồn kho.

---

## 8. KẾ HOẠCH THỰC HIỆN

| Giai đoạn | Nội dung công việc |
|---|---|
| **Giai đoạn 1** | **Phân tích yêu cầu:** Khảo sát yêu cầu e-commerce và xác định các kịch bản sử dụng Trợ lý AI (User & Admin). |
| **Giai đoạn 2** | **Thiết kế hệ thống:** Figma UI/UX (gồm AI Chat Widget), Diagram (Use Case, Sequence, Class) & DB Design. |
| **Giai đoạn 3** | **Phát triển Backend & AI:** Lập trình REST API (Product, Cart, Order, Inventory, Auth) & Tích hợp Spring AI / Function Calling query DB. |
| **Giai đoạn 4** | **Phát triển Frontend:** Giao diện Nuxt 3, bộ lọc thông số linh kiện, Cart, Checkout, Admin Dashboard & UI AI Chatbot. |
| **Giai đoạn 5** | **Tích hợp & Kiểm thử:** Kết nối FE-BE, test luồng mua hàng, phân quyền AI Admin/User và kiểm thử độ chính xác truy vấn số liệu của AI Bot. |
| **Giai đoạn 6** | **Hoàn thiện:** Tối ưu hiệu năng, sửa lỗi, đóng gói ứng dụng và hoàn thiện báo cáo đồ án. |

---

## 9. PHÂN CÔNG CÔNG VIỆC

| Thành viên | Tỷ lệ đóng góp | Trách nhiệm chính |
|---|---|---|
| **Đàm Anh Pháp** | **50%** | **UI/UX, Frontend & AI Interface:**<br>• Xây dựng Frontend bằng Nuxt 3.<br>• Thiết kế UI/UX Storefront, Product List, Detail, Cart, Checkout, Admin Dashboard.<br>• Xây dựng thành phần **AI Chatbot Widget** (khung chat, gợi ý câu hỏi mẫu, giao diện hội thoại giọng nói/văn bản).<br>• Tích hợp Frontend với Backend API & kiểm thử giao diện. |
| **Phạm Đức Tài** | **50%** | **Phân tích thiết kế, Backend & AI Engine:**<br>• Phân tích yêu cầu, thiết kế kiến trúc Use Case, Sequence, Class Diagram.<br>• Xây dựng Backend bằng Spring Boot, REST API (Product, Variant, Cart, Order, Inventory).<br>• Triển khai **AI Engine Service (Spring AI / LLM API + Function Calling)** để Bot truy vấn dữ liệu CSDL (Doanh thu, Tồn kho, Bán chạy).<br>• Kiểm thử API, bảo mật phân quyền AI & hoàn thiện báo cáo dự án. |
| **Tổng cộng** | **100%** | |

---

## 10. KẾT QUẢ DỰ KIẾN

* **Hệ thống E-Commerce AI hoàn chỉnh:** Gồm Storefront cho khách hàng, Admin Dashboard cho quản trị viên và **Trợ lý AI thông minh** đa nhiệm.
* **Điểm đột phá của Đồ án:**
  * Khách hàng lọc linh kiện dễ dàng theo thuộc tính kỹ thuật và nhận tư vấn trực tiếp từ AI Bot.
  * Admin theo dõi số liệu kinh doanh linh hoạt thông qua biểu đồ và truy vấn tức thì bằng AI Bot (*"Doanh thu hôm nay bao nhiêu?"*).
* **Công nghệ hiện đại:** Nuxt 3, Tailwind CSS, Pinia, Spring Boot 4, Spring Security, Spring AI / LLM API (Function Calling), PostgreSQL.
* **Hàm lượng chuyên môn cao:** Thể hiện được khả năng tích hợp công nghệ AI mới vào bài toán Thương mại điện tử thực tế.

---

## 11. KẾT LUẬN

Dự án **“Xây dựng Website bán linh kiện điện tử tích hợp Trợ lý AI”** không chỉ giải quyết bài toán mua sắm linh kiện theo thông số kỹ thuật đặc thù, mà còn nâng tầm đồ án nhờ ứng dụng Trí tuệ nhân tạo (AI) vào quản trị kinh doanh và tư vấn khách hàng.

Giải pháp sử dụng **Nuxt 3 + Spring Boot 4 + PostgreSQL + Spring AI / LLM Function Calling** tạo nên một hệ thống vững chắc, hiện đại và đạt tiêu chuẩn cao của một đồ án tốt nghiệp.

Sự phối hợp chặt chẽ **50% - 50%** giữa **Đàm Anh Pháp** (UI/UX, Frontend & AI Widget) và **Phạm Đức Tài** (Backend, Database, System Architecture & AI Engine) đảm bảo tiến độ và chất lượng hoàn thành dự án ở mức tối ưu.
