# Hệ Thống Thương Mại Điện Tử - Linh Kiện Điện Tử

Nền tảng thương mại điện tử full-stack hiện đại chuyên về linh kiện điện tử, được xây dựng với Spring Boot và Nuxt.js.

## Mục Lục

- [Giới Thiệu](#giới-thiệu)
- [Tính Năng](#tính-năng)
- [Công Nghệ Sử Dụng](#công-nghệ-sử-dụng)
- [Yêu Cầu Hệ Thống](#yêu-cầu-hệ-thống)
- [Hướng Dẫn Cài Đặt](#hướng-dẫn-cài-đặt)
- [Cấu Hình](#cấu-hình)
- [Chạy Ứng Dụng](#chạy-ứng-dụng)
- [Cấu Trúc Dự Án](#cấu-trúc-dự-án)
- [Tài Liệu API](#tài-liệu-api)

## Giới Thiệu

Hệ thống thương mại điện tử linh kiện điện tử là một nền tảng mua sắm trực tuyến toàn diện được thiết kế để mua bán các linh kiện điện tử. Hệ thống cung cấp trải nghiệm mua sắm liền mạch với các tính năng như quản lý sản phẩm, theo dõi đơn hàng, tích hợp thanh toán an toàn và xác thực người dùng.

## Tính Năng

- **Xác Thực & Phân Quyền**: Đăng ký và đăng nhập an toàn với JWT tokens
- **Quản Lý Sản Phẩm**: Duyệt, tìm kiếm và lọc linh kiện điện tử
- **Giỏ Hàng**: Thêm sản phẩm vào giỏ với lựa chọn biến thể
- **Tích Hợp Thanh Toán**: Hỗ trợ cổng thanh toán MoMo
- **Quản Lý Đơn Hàng**: Theo dõi đơn hàng và lịch sử mua hàng
- **Giao Hàng Smart Locker**: Tích hợp hệ thống giao hàng tủ thông minh
- **Hồ Sơ Người Dùng**: Quản lý thông tin cá nhân và địa chỉ
- **Trang Quản Trị**: Bảng điều khiển toàn diện để quản lý sản phẩm, đơn hàng, người dùng và danh mục
- **Thiết Kế Responsive**: Giao diện thân thiện với mobile sử dụng Tailwind CSS

## Công Nghệ Sử Dụng

### Backend
- **Java 21** - Ngôn ngữ lập trình
- **Spring Boot 4.0.1** - Framework ứng dụng
- **Spring Security** - Xác thực và phân quyền
- **Spring Data JPA** - Quản lý dữ liệu
- **PostgreSQL 15** - Cơ sở dữ liệu chính
- **Redis 8.0** - Caching và quản lý session
- **Maven** - Công cụ build
- **Docker** - Containerization

### Frontend
- **Nuxt.js 4.2** - Framework Vue.js
- **Vue 3** - Progressive JavaScript framework
- **TypeScript** - JavaScript có kiểu dữ liệu
- **Tailwind CSS 4.0** - Utility-first CSS framework
- **Pnpm** - Trình quản lý package

## Yêu Cầu Hệ Thống

Trước khi bắt đầu, đảm bảo bạn đã cài đặt:

- **Java Development Kit (JDK) 21** trở lên
- **Maven 3.8+** hoặc sử dụng Maven wrapper đi kèm
- **Node.js 18+** và **pnpm**
- **Docker** và **Docker Compose** (để thiết lập cơ sở dữ liệu)
- **Git** để quản lý phiên bản
- **PostgreSQL 15** (tùy chọn nếu dùng Docker)
- **Redis** (tùy chọn nếu dùng Docker)

## Hướng Dẫn Cài Đặt

### 1. Clone Repository

```bash
git clone https://github.com/your-username/E-commerce-Electronic-Components.git
cd E-commerce-Electronic-Components
```

### 2. Cài Đặt Backend

#### Bước 2.1: Khởi Động Database với Docker

Di chuyển đến thư mục backend và khởi động PostgreSQL và Redis bằng Docker Compose:

```bash
cd BE/E-commerce-Electronic-Components-Backend
docker-compose up -d
```

Lệnh này sẽ khởi động:
- PostgreSQL trên cổng `5432`
- Redis trên cổng `6379`

#### Bước 2.2: Cấu Hình Biến Môi Trường

Tạo file `.env` trong thư mục backend:

```bash
cd BE/E-commerce-Electronic-Components-Backend
# Tạo file .env với cấu hình của bạn
```

Ví dụ cấu hình `.env`:
```properties
# Cấu hình Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/test_api
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=123321

# Cấu hình Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# Cấu hình JWT
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000

# Cổng ứng dụng
SERVER_PORT=8080
```

#### Bước 2.3: Import Database Schema

Import schema SQL vào PostgreSQL:

```bash
# Sử dụng psql
psql -U postgres -d test_api -f database/sql.sql

# Hoặc sử dụng Docker
docker exec -i ecommerce-postgres psql -U postgres -d test_api < database/sql.sql
```

#### Bước 2.4: Build và Chạy Backend

Sử dụng Maven wrapper:

```bash
# Windows
.\mvnw clean install
.\mvnw spring-boot:run

# Linux/Mac
./mvnw clean install
./mvnw spring-boot:run
```

Hoặc sử dụng Maven trực tiếp:

```bash
mvn clean install
mvn spring-boot:run
```

Backend server sẽ khởi động tại `http://localhost:8080`

### 3. Cài Đặt Frontend

#### Bước 3.1: Cài Đặt Dependencies

Di chuyển đến thư mục frontend và cài đặt dependencies:

```bash
cd frontend
pnpm install
```

#### Bước 3.2: Cấu Hình Environment

Tạo file `.env` trong thư mục frontend:

```bash
# Cấu hình API
NUXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
```

#### Bước 3.3: Chạy Frontend Development Server

```bash
pnpm dev
```

Ứng dụng frontend sẽ khởi động tại `http://localhost:3000`

## Cấu Hình

### File Cấu Hình Backend

- **application.yaml**: Cấu hình ứng dụng chính
- **application-dev.yaml**: Cấu hình môi trường phát triển
- **application-test.properties**: Cấu hình môi trường test

### File Cấu Hình Frontend

- **nuxt.config.ts**: Cấu hình Nuxt.js
- **tailwind.config.ts**: Cấu hình Tailwind CSS
- **tsconfig.json**: Cấu hình TypeScript

## Chạy Ứng Dụng

### Chế Độ Development

1. **Khởi động Docker services** (PostgreSQL & Redis):
   ```bash
   cd BE/E-commerce-Electronic-Components-Backend
   docker-compose up -d
   ```

2. **Khởi động Backend**:
   ```bash
   cd BE/E-commerce-Electronic-Components-Backend
   ./mvnw spring-boot:run
   ```

3. **Khởi động Frontend**:
   ```bash
   cd frontend
   pnpm dev
   ```

4. **Truy cập ứng dụng**:
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - Tài liệu API: http://localhost:8080/swagger-ui.html (nếu được cấu hình)

### Build Production

#### Backend
```bash
cd BE/E-commerce-Electronic-Components-Backend
./mvnw clean package
java -jar target/E-commerce-Electronic-Components-Backend-0.0.1-SNAPSHOT.jar
```

#### Frontend
```bash
cd frontend
pnpm build
pnpm preview
```

## Cấu Trúc Dự Án

```
E-commerce-Electronic-Components/
├── BE/
│   └── E-commerce-Electronic-Components-Backend/
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/com/example/ecommerceelectroniccomponentsbackend/
│       │   │   │   ├── config/          # Các class cấu hình
│       │   │   │   ├── controller/      # REST controllers
│       │   │   │   ├── dto/             # Data Transfer Objects
│       │   │   │   ├── entity/          # JPA entities
│       │   │   │   ├── exception/       # Exception handlers
│       │   │   │   ├── mapper/          # Entity-DTO mappers
│       │   │   │   ├── repository/      # Data repositories
│       │   │   │   └── service/         # Business logic
│       │   │   └── resources/
│       │   │       ├── application.yaml
│       │   │       └── application-dev.yaml
│       │   └── test/                    # Unit và integration tests
│       ├── database/
│       │   └── sql.sql                  # Database schema
│       ├── docker-compose.yaml          # Docker services
│       └── pom.xml                      # Maven dependencies
│
└── frontend/
    ├── app/
    │   ├── components/                  # Các Vue component tái sử dụng
    │   ├── composables/                 # Vue composables
    │   ├── layouts/                     # Page layouts
    │   ├── middleware/                  # Route middleware
    │   ├── pages/                       # Các trang ứng dụng
    │   │   ├── admin/                   # Trang quản trị
    │   │   ├── auth/                    # Trang xác thực
    │   │   ├── payment/                 # Trang thanh toán
    │   │   └── products/                # Trang sản phẩm
    │   └── types/                       # TypeScript type definitions
    ├── public/                          # Static assets
    ├── nuxt.config.ts                   # Cấu hình Nuxt
    └── package.json                     # Node dependencies
```

## Tài Liệu API

API endpoints được tổ chức theo các nhóm sau:

- **Xác thực**: `/api/auth/*` - Đăng nhập, đăng ký, đặt lại mật khẩu
- **Sản phẩm**: `/api/products/*` - Các thao tác CRUD sản phẩm
- **Danh mục**: `/api/categories/*` - Quản lý danh mục
- **Đơn hàng**: `/api/orders/*` - Xử lý và theo dõi đơn hàng
- **Giỏ hàng**: `/api/cart/*` - Các thao tác giỏ hàng
- **Người dùng**: `/api/users/*` - Quản lý người dùng
- **Thanh toán**: `/api/payment/*` - Xử lý thanh toán

Để xem tài liệu API chi tiết, truy cập Swagger UI tại `http://localhost:8080/swagger-ui.html` (nếu được cấu hình).

## Testing

### Backend Tests
```bash
cd BE/E-commerce-Electronic-Components-Backend
./mvnw test
```

### Frontend Tests
```bash
cd frontend
pnpm test
```

## Xử Lý Sự Cố

### Các Vấn Đề Thường Gặp

1. **Cổng đã được sử dụng**:
   - Backend: Thay đổi `server.port` trong `application.yaml`
   - Frontend: Thay đổi cổng trong `nuxt.config.ts` hoặc dùng `pnpm dev --port 3001`

2. **Kết nối database thất bại**:
   - Đảm bảo Docker containers đang chạy: `docker-compose ps`
   - Kiểm tra thông tin đăng nhập database trong file `.env`

3. **Maven build thất bại**:
   - Xóa Maven cache: `./mvnw clean`
   - Cập nhật dependencies: `./mvnw dependency:resolve`

4. **Lỗi build Frontend**:
   - Xóa node modules: `rm -rf node_modules && pnpm install`
   - Xóa Nuxt cache: `rm -rf .nuxt`

## Giấy Phép

Dự án này được cấp phép theo giấy phép MIT - xem file LICENSE để biết thêm chi tiết.

## Đóng Góp

- Nhóm phát triển

## Liên Hệ

Để được hỗ trợ hoặc có câu hỏi, vui lòng liên hệ với nhóm phát triển.

---

**Lưu ý**: Đây là dự án giáo dục phục vụ mục đích học tập.