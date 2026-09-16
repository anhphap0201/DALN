# ĐỀ XUẤT DỰ ÁN: XÂY DỰNG WEBSITE BÁN LINH KIỆN ĐIỆN TỬ TÍCH HỢP TRỢ LÝ AI

---

## 1. THÔNG TIN DỰ ÁN

* **Tên dự án:** Xây dựng Website bán linh kiện điện tử tích hợp Trợ lý AI
* **Lĩnh vực:** Thương mại điện tử (E-commerce) & Trí tuệ nhân tạo (AI)
* **Mô hình:** B2C (Business-to-Consumer)
* **Đối tượng sử dụng:** Sinh viên, kỹ sư, người học kỹ thuật, khách hàng có nhu cầu mua linh kiện và Quản trị viên hệ thống (Admin).

---

## 2. GIỚI THIỆU DỰ ÁN

Dự án **“Xây dựng Website bán linh kiện điện tử tích hợp Trợ lý AI”** được triển khai nhằm xây dựng một hệ thống thương mại điện tử chuyên cung cấp các sản phẩm linh kiện điện tử và cơ điện tử.

Khác với các sản phẩm tiêu dùng thông thường, linh kiện điện tử sở hữu nhiều thuộc tính kỹ thuật đặc thù (*điện áp, điện dung, công suất, sai số, số chân IC, kiểu đóng gói,...*). Khách hàng đòi hỏi khả năng tìm kiếm chính xác theo thông số, trong khi nhà quản trị cần theo dõi nhanh chóng các số liệu kinh doanh. Hệ thống tập trung giải quyết 2 bài toán cốt lõi:
1. **Tìm kiếm và lọc sản phẩm theo thuộc tính kỹ thuật chi tiết.**
2. **Tích hợp Trợ lý AI thông minh (AI Bot Assistant):** Hỗ trợ Quản trị viên truy vấn báo cáo doanh thu, tồn kho, sản phẩm bán chạy bằng ngôn ngữ tự nhiên; đồng thời tư vấn kỹ thuật và hỗ trợ tra cứu cho khách hàng.

---

## 3. MỤC TIÊU DỰ ÁN

* Xây dựng website thương mại điện tử bán linh kiện điện tử với giao diện trực quan, tối ưu trải nghiệm người dùng (UX/UI).
* Xây dựng bộ lọc thông số kỹ thuật đa chiều theo từng loại linh kiện.
* Cung cấp đầy đủ các chức năng thương mại điện tử: xem chi tiết sản phẩm, quản lý biến thể (SKU), giỏ hàng, đặt hàng và thanh toán.
* Hỗ trợ phương thức đặt hàng không cần đăng nhập (**Guest Checkout**) và chức năng theo dõi đơn hàng.
* Xây dựng phân hệ quản trị (Admin Dashboard) quản lý sản phẩm, danh mục, tồn kho, đơn hàng và báo cáo thống kê.
* **Tích hợp Trợ lý AI (AI Chatbot) đa nhiệm:**
  * **Đối với Quản trị viên:** Hỏi đáp dữ liệu doanh thu, thống kê sản phẩm bán chạy và cảnh báo tồn kho bằng ngôn ngữ tự nhiên.
  * **Đối với Khách hàng:** Tư vấn lựa chọn linh kiện theo yêu cầu kỹ thuật và hỗ trợ tra cứu đơn hàng.
* Triển khai cơ chế xác thực, phân quyền (**Spring Security & JWT**) và bảo mật dữ liệu theo chuẩn ngành.

---

## 4. PHẠM VI VÀ CHỨC NĂNG CHÍNH

### 4.1. Chức năng dành cho Khách hàng

* **Xem và tìm kiếm sản phẩm:**
  * Xem sản phẩm theo danh mục/thương hiệu.
  * Tìm kiếm theo tên, từ khóa hoặc mã linh kiện.
* **Lọc sản phẩm theo thông số kỹ thuật (Chức năng trọng tâm):**
  * *Điện trở:* Công suất (Watt), sai số (%), giá trị điện trở (Ohm).
  * *Tụ điện:* Điện dung (uF/pF), điện áp chịu đựng (V), loại tụ (gốm/hóa/tần).
  * *Vi mạch (IC):* Số chân, loại IC, dạng đóng gói (DIP/SMD).
* **Xem chi tiết sản phẩm:**
  * Hình ảnh, giá bán, mô tả sản phẩm.
  * Bảng thông số kỹ thuật chi tiết.
  * Lựa chọn biến thể / SKU và tình trạng tồn kho thực tế.
* **Giỏ hàng và Đặt hàng:**
  * Thêm, sửa, xóa sản phẩm trong giỏ hàng.
  * Hỗ trợ **Guest Checkout** cho khách hàng không đăng nhập tài khoản.
* **Thanh toán:**
  * Thanh toán khi nhận hàng (**COD**).
  * Mô phỏng thanh toán trực tuyến qua ví điện tử.
* **Theo dõi đơn hàng:**
  * Tra cứu tiến trình xử lý và trạng thái đơn hàng thông qua mã đơn.

### 4.2. Chức năng dành cho Quản trị viên (Admin)

* **Quản lý danh mục & Sản phẩm:** Quản lý sản phẩm, thương hiệu, danh mục và mã SKU duy nhất cho từng biến thể.
* **Quản lý tồn kho:** Cập nhật số lượng nhập/xóa, thiết lập ngưỡng cảnh báo tồn kho tối thiểu.
* **Quản lý đơn hàng:** Tiếp nhận, cập nhật trạng thái đơn hàng (Chờ xác nhận, Đang xử lý, Đang giao, Đã hoàn thành, Hủy).
* **Báo cáo & Thống kê:** Biểu đồ doanh thu theo thời gian, số lượng đơn hàng, top sản phẩm bán chạy.
* **Quản lý người dùng:** Quản lý tài khoản và phân quyền hệ thống (`ROLE_USER`, `ROLE_ADMIN`).

### 4.3. Chức năng Trợ lý AI (AI Bot Assistant)

Phân hệ Trợ lý AI được phát triển nhằm tự động hóa công tác tư vấn và hỗ trợ ra quyết định kinh doanh:

* **Phân hệ AI dành cho Quản trị viên (AI Business Intelligence):**
  * **Hỏi đáp doanh thu:** *"Doanh thu hôm nay/tháng này là bao nhiêu?"*, *"So sánh doanh thu tháng này với tháng trước"*.
  * **Thống kê sản phẩm:** *"Sản phẩm nào có doanh số cao nhất trong tháng?"*, *"Top 5 linh kiện bán chạy nhất"*.
  * **Cảnh báo tồn kho:** *"Liệt kê các linh kiện có số lượng tồn kho dưới 10"*, *"Sản phẩm nào cần nhập bổ sung?"*.
  * **Tổng hợp số liệu:** Phản hồi số liệu tự động dưới dạng văn bản tổng hợp hoặc bảng dữ liệu.

* **Phân hệ AI dành cho Khách hàng (AI Technical Advisor):**
  * **Tư vấn kỹ thuật:** *"Tôi cần mạch hạ áp từ 12V xuống 5V thì dùng IC nào?"*, *"Tụ điện 16V có chịu được nguồn 24V không?"*.
  * **Gợi ý linh kiện:** Gợi ý danh sách linh kiện phù hợp đang có tại cửa hàng.
  * **Tra cứu đơn hàng:** *"Kiểm tra trạng thái đơn hàng mã #ORD-88219"*.

---

## 5. CÔNG NGHỆ VÀ KIẾN TRÚC HỆ THỐNG

### 5.1. Công nghệ sử dụng

#### **Frontend**
* **Nuxt 3 (Vue 3 Framework):** Xây dựng giao diện ứng dụng (Storefront, Filter System, Cart, Admin Dashboard, AI Chat Widget).
* **Tailwind CSS:** Thiết kế giao diện UI đồng bộ, responsive.
* **Pinia:** Quản lý trạng thái ứng dụng (User Auth, Cart, Chat History, Tokens).

#### **Backend & AI Engine**
* **Java Spring Boot 4:** Xây dựng hệ thống RESTful API và xử lý nghiệp vụ kinh doanh.
* **Spring Security & JWT:** Bảo vệ hệ thống bằng Access Token & Refresh Token, mã hóa mật khẩu bằng `BCryptPasswordEncoder`.
* **Spring AI / OpenAI API / Gemini API**
  * Xử lý ngôn ngữ tự nhiên (NLP) cho Trợ lý AI.
  * Áp dụng kỹ thuật **Function Calling (Tool Calling)** cho phép AI truy vấn dữ liệu trực tiếp từ PostgreSQL một cách an toàn và chính xác.
* **Hibernate / JPA:** Kết nối và thao tác với cơ sở dữ liệu qua các Entity.

#### **Cơ sở dữ liệu & Công cụ phát triển**
* **Cơ sở dữ liệu:** PostgreSQL.
* **Công cụ quản lý & API:** Git / GitHub, Swagger / OpenAPI.
* **Thiết kế UI/UX & Quản lý tiến độ:** Figma, Trello.

---

## 6. KIẾN TRÚC HỆ THỐNG TÍCH HỢP AI

Backend được thiết kế theo **Kiến trúc phân tầng 3 lớp (Layered Architecture)** kết hợp phân hệ **AI Engine Service**:

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

**Quy trình xử lý truy vấn AI:**
1. Người dùng/Admin gửi câu hỏi bằng ngôn ngữ tự nhiên tới AI Controller.
2. AI Engine phân tích ý định, kích hoạt **Function Calling** tương ứng (ví dụ: `getRevenueByDateRange()`).
3. Service Layer truy vấn dữ liệu từ PostgreSQL thông qua Repository.
4. Đại mô hình ngôn ngữ (LLM) tổng hợp số liệu thực tế từ CSDL và phản hồi kết quả chính xác cho người dùng.

---

## 7. BẢO MẬT VÀ YÊU CẦU NGHỆP VỤ

### Bảo mật & Quyền truy cập AI
* Xác thực **JWT**, mã hóa mật khẩu **BCrypt**, phân quyền `ROLE_USER` và `ROLE_ADMIN`.
* **Phân quyền dữ liệu AI:** Chỉ tài khoản có quyền `ROLE_ADMIN` mới được phép truy vấn dữ liệu doanh thu, báo cáo tài chính và tồn kho. Khách hàng chỉ có quyền sử dụng AI tư vấn kỹ thuật và tra cứu đơn hàng cá nhân.
* Kiểm tra dữ liệu đầu vào (Input Validation), phòng chống các lỗ hổng bảo mật Web và Prompt Injection.

### Yêu cầu nghiệp vụ
* Mã SKU của sản phẩm là duy nhất trên toàn hệ thống.
* Kiểm tra tồn kho trước khi xác nhận đơn hàng.
* Trợ lý AI phản hồi dựa trên dữ liệu thực tế từ CSDL, đảm bảo độ chính xác của báo cáo.

---

## 8. KẾ HOẠCH THỰC HIỆN

| Giai đoạn | Nội dung công việc |
|---|---|
| **Giai đoạn 1** | **Phân tích yêu cầu:** Xác định mục tiêu, phạm vi và xây dựng các kịch bản sử dụng hệ thống & Trợ lý AI. |
| **Giai đoạn 2** | **Thiết kế hệ thống:** Thiết kế UI/UX Figma, xây dựng Use Case, Activity, Sequence, Class Diagram & DB Schema. |
| **Giai đoạn 3** | **Phát triển Backend & AI:** Xây dựng CSDL, REST API (Product, Cart, Order, Inventory, Auth) và triển khai AI Function Calling. |
| **Giai đoạn 4** | **Phát triển Frontend:** Lập trình Nuxt 3 cho các trang Storefront, bộ lọc thông số, Cart, Checkout, Admin Dashboard & UI AI Chat. |
| **Giai đoạn 5** | **Tích hợp & Kiểm thử:** Kết nối FE-BE, kiểm thử tích hợp, kiểm tra độ chính xác của các truy vấn AI và phân quyền. |
| **Giai đoạn 6** | **Hoàn thiện:** Sửa lỗi, tối ưu hiệu năng, đóng gói hệ thống và hoàn thiện tài liệu báo cáo dự án. |

---

## 9. PHÂN CÔNG CÔNG VIỆC

| Thành viên | Tỷ lệ đóng góp | Trách nhiệm chính |
|---|---|---|
| **Đàm Anh Pháp** | **50%** | **UI/UX, Frontend & Giao diện AI:**<br>• Xây dựng ứng dụng Frontend bằng Nuxt 3.<br>• Thiết kế UI/UX Storefront, Product List, Detail, Cart, Checkout, Admin Dashboard.<br>• Xây dựng thành phần **AI Chatbot Widget** (giao diện hội thoại, câu hỏi mẫu).<br>• Kết nối Frontend với Backend API & kiểm thử giao diện. |
| **Phạm Đức Tài** | **50%** | **Phân tích thiết kế, Backend & AI Engine:**<br>• Phân tích yêu cầu, thiết kế Use Case, Sequence, Class Diagram.<br>• Xây dựng Backend với Spring Boot, REST API (Product, Variant, Cart, Order, Inventory).<br>• Triển khai **AI Engine Service (Spring AI / LLM API + Function Calling)** xử lý truy vấn doanh thu, tồn kho từ CSDL.<br>• Kiểm thử API, bảo mật phân quyền AI & hoàn thiện tài liệu báo cáo. |
| **Tổng cộng** | **100%** | |

---

## 10. KẾT QUẢ DỰ KIẾN

* **Hệ thống E-Commerce tích hợp AI hoàn chỉnh:** Gồm Storefront dành cho khách hàng, Admin Dashboard dành cho quản trị viên và Trợ lý AI đa nhiệm.
* **Tính năng ứng dụng thực tiễn:** Khách hàng lọc sản phẩm theo thuộc tính kỹ thuật và nhận tư vấn từ AI Bot; Quản trị viên truy vấn báo cáo doanh thu và tồn kho nhanh chóng.
* **Làm chủ công nghệ:** Nuxt 3, Tailwind CSS, Pinia ở Frontend; Spring Boot 4, Spring Security, Spring AI / LLM Function Calling, PostgreSQL ở Backend.

---

## 11. KẾT LUẬN

Dự án **“Xây dựng Website bán linh kiện điện tử tích hợp Trợ lý AI”** giải quyết bài toán tìm kiếm và lựa chọn linh kiện theo thuộc tính kỹ thuật đặc thù, đồng thời ứng dụng công nghệ Trí tuệ nhân tạo (AI) trong công tác quản trị kinh doanh và tư vấn khách hàng.

Hệ thống được phát triển trên nền tảng **Nuxt 3, Spring Boot 4, PostgreSQL và Spring AI**, đáp ứng các tiêu chuẩn về kiến trúc, bảo mật và khả năng mở rộng của một đồ án tốt nghiệp.

Sự phân công công việc cân bằng **50% - 50%** giữa **Đàm Anh Pháp** (Frontend/UI/UX/AI Interface) và **Phạm Đức Tài** (Backend/DB/Architecture/AI Engine) bảo đảm tính hiệu quả và tiến độ hoàn thành dự án.
