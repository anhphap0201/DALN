# THIẾT KẾ FRONTEND (FRONTEND ARCHITECTURE & UI RULES)

## 1. Cấu Trúc Tổng Thể & Tech Stack
- **Framework:** Nuxt 4.2.2 (Vue 3 Composition API, `<script setup lang="ts">`).
- **Styling:** Tailwind CSS 4.0.
- **Ngôn ngữ:** TypeScript chuẩn Strict.
- **Thư mục mã nguồn:** `frontend/app/`

```
├── assets/             # CSS toàn cục (main.css) và static assets
├── components/         # Các UI components tái sử dụng
├── composables/        # State management và business logic dùng chung
├── layouts/            # Layout khung giao diện (default.vue)
├── middleware/         # Nuxt route guards (auth.ts, admin.ts, guest.ts)
├── pages/              # Các trang theo cơ chế file-based routing của Nuxt
└── types/              # Khai báo TypeScript types / interfaces
```

## 2. Quy Tắc UI & Thiết Kế (UI & Design Standards)
1. **Responsive First:** Giao diện tối ưu hoàn hảo từ màn hình di động (mobile) đến máy tính để bàn (desktop) qua hệ thống breakpoint của Tailwind CSS.
2. **Component Độc Lập & Nhất Quán:**
   - Dùng modal thông báo & xác nhận dùng chung (`ConfirmModal.vue`, `NotificationContainer.vue`).
   - Bong bóng Trợ lý AI (`ChatWidget.vue`) cố định góc phải màn hình, hỗ trợ tư vấn và tra cứu tồn kho qua Ollama.
   - Thẻ hiển thị sản phẩm đồng nhất (`ProductCard.vue`), tự động hiển thị dải giá biến thể và huy hiệu tồn kho.
   - Chọn biến thể linh kiện thông qua modal chuyên dụng (`VariantSelectorModal.vue`).
3. **Form & Validation:**
   - Trực quan hóa trạng thái lỗi (input border đỏ, thông điệp lỗi bên dưới trường nhập).
   - Disable nút Submit trong khi request đang gửi để chống duplicate submit.

## 3. Hệ Thống Trang (Pages)
- **Cửa hàng & Người dùng thông thường:**
  - `pages/index.vue`: Trang chủ hiển thị danh mục nổi bật, sản phẩm mới nhất, khuyến mãi.
  - `pages/products/[id].vue`: Chi tiết linh kiện, chọn biến thể, thông số kỹ thuật chi tiết.
  - `pages/search.vue`: Tìm kiếm theo từ khóa và lọc nâng cao theo thuộc tính.
  - `pages/cart.vue`: Xem giỏ hàng, cập nhật số lượng, xóa sản phẩm.
  - `pages/checkout.vue`: Đặt hàng, chọn hình thức giao (Smart Locker / Tận nơi), chọn phương thức thanh toán.
  - `pages/orders.vue`: Lịch sử và trạng thái các đơn hàng đã đặt.
  - `pages/profile.vue`: Quản lý thông tin cá nhân và sổ địa chỉ.
- **Xác thực:**
  - `pages/auth/auth.vue`: Đăng nhập & Đăng ký tài khoản.
  - `pages/auth/forgot-password.vue`: Gửi yêu cầu đặt lại mật khẩu.
  - `pages/auth/reset-password.vue`: Đặt mật khẩu mới qua token.
  - `pages/verify-email.vue`: Xác thực email đăng ký.
  - `pages/auth/logout.vue`: Xử lý đăng xuất và dọn dẹp state/cookie.
- **Thanh toán:**
  - `pages/payment/momo-return.vue`: Tiếp nhận callback điều hướng sau khi thanh toán MoMo hoàn tất.
- **Trang Quản Trị (Admin Dashboard - Yêu cầu role ADMIN):**
  - `pages/admin/index.vue`: Bảng tổng quan số liệu thống kê.
  - `pages/admin/products.vue`: CRUD sản phẩm, thông số kỹ thuật và biến thể.
  - `pages/admin/categories.vue`: Quản lý danh mục linh kiện.
  - `pages/admin/orders.vue`: Quản lý và duyệt trạng thái đơn hàng.
  - `pages/admin/users.vue`: Quản lý danh sách người dùng và phân quyền.

## 4. Composables & State Management
- **`useAuth.ts`:** Lưu trữ thông tin `user`, token, hàm `login`, `register`, `logout`, `fetchCurrentUser`.
- **`useCart.ts`:** Quản lý danh sách mặt hàng trong giỏ, tính tổng tiền, đồng bộ giỏ hàng với server.
- **`useNotification.ts`:** Hệ thống toast notification toàn cục (thành công, cảnh báo, lỗi).
- **`useConfirm.ts`:** Modal xác nhận thao tác nguy hiểm (xóa sản phẩm, hủy đơn).
- **`useSmartLocker.ts`:** Lấy danh sách điểm đặt tủ thông minh và quản lý ngăn tủ.
- **`useProvinces.ts`:** Cung cấp dữ liệu tỉnh/thành phố, quận/huyện phục vụ giao hàng.

## 5. Route Middleware
- `auth.ts`: Chặn người dùng chưa đăng nhập, chuyển hướng về `/auth/auth`.
- `guest.ts`: Chặn người dùng đã đăng nhập vào lại các trang login/register.
- `admin.ts`: Chỉ cho phép người dùng có `ROLE_ADMIN` truy cập các route `/admin/**`.
