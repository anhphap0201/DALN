DROP TABLE IF EXISTS user_notifications CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS articles CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS cart_items CASCADE;
DROP TABLE IF EXISTS carts CASCADE;
DROP TABLE IF EXISTS product_variants CASCADE;
DROP TABLE IF EXISTS product_categories CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255), -- bcrypt hash
    full_name VARCHAR(255),
    phone VARCHAR(20),
    province VARCHAR(255),
    ward VARCHAR(255),
    detailed_address VARCHAR(500),
    gender VARCHAR(10),
    birth_date DATE,
    role VARCHAR(50) DEFAULT 'ROLE_USER',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    image_url TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    avg_rating DOUBLE PRECISION DEFAULT 0,
    sold_quantity INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE product_categories (
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, category_id)
);

CREATE TABLE product_variants (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    variant_name VARCHAR(255),
    description TEXT,
    specifications TEXT,
    image_url TEXT,
    price NUMERIC(12,2) NOT NULL,
    discount_price NUMERIC(12,2),
    in_stock INT DEFAULT 0 CHECK (in_stock >= 0),
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT REFERENCES carts(id) ON DELETE CASCADE,
    product_variant_id BIGINT REFERENCES product_variants(id) ON DELETE CASCADE,
    quantity INT NOT NULL CHECK (quantity > 0)
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    total_cost NUMERIC(12,2) NOT NULL,
    shipping_cost NUMERIC(12,2),
    payment_method VARCHAR(50),
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    product_variant_id BIGINT REFERENCES product_variants(id) ON DELETE SET NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12,2) NOT NULL
);

CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, product_id)
);

CREATE TABLE articles (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    title VARCHAR(255),
    slug VARCHAR(255) UNIQUE,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255),
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_notifications (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    notification_id BIGINT REFERENCES notifications(id) ON DELETE CASCADE,
    is_read BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (user_id, notification_id)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_products_slug ON products(slug);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_reviews_product ON reviews(product_id);

-- =====================================================
-- DỮ LIỆU MẪU (SAMPLE DATA)
-- =====================================================

-- Người dùng mẫu (password: admin123 và user123)
INSERT INTO users (email, password, full_name, phone, gender, role, is_active) VALUES
('he@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', '0901234567', 'Male', 'ROLE_ADMIN', TRUE),
('user@gmail.com', '$2a$10$DOwnh2KYn6L9LPSt1mGULe5VYU6L4W1W1QnJZ/6nT9LxRr6mYY.Ym', 'Nguyễn Văn A', '0987654321', 'Male', 'ROLE_USER', TRUE),
('maria@gmail.com', '$2a$10$DOwnh2KYn6L9LPSt1mGULe5VYU6L4W1W1QnJZ/6nT9LxRr6mYY.Ym', 'Trần Thị B', '0912345678', 'Female', 'ROLE_USER', TRUE);

-- Danh mục sản phẩm
INSERT INTO categories (name, slug, image_url, description) VALUES
('Arduino & Modules', 'arduino-modules', '/images/ArduinoUnoR3.jpg', 'Bo mạch Arduino và các module mở rộng'),
('Raspberry Pi', 'raspberry-pi', '/images/raspberry-pi-5-6.jpg', 'Máy tính nhúng Raspberry Pi và phụ kiện'),
('Linh kiện điện tử', 'linh-kien-dien-tu', '/images/resistores.jpg', 'Điện trở, tụ điện, transistor, IC và các linh kiện cơ bản'),
('Cảm biến', 'cam-bien', '/images/cam-bien-vat-can-hong-ngoai-fm52-5.jpg', 'Các loại cảm biến nhiệt độ, độ ẩm, khoảng cách, ánh sáng'),
('Module nguồn', 'module-nguon', '/images/pin-18650__T7hrNFf7Uz.webp', 'Module nguồn, sạc pin, Buck/Boost converter'),
('Module truyền thông', 'module-truyen-thong', '/images/nodemcu-esp32-01.webp', 'WiFi, Bluetooth, LoRa, RF, GSM modules'),
('Màn hình hiển thị', 'man-hinh-hien-thi', '/images/man-hinh-lcd-oled-0-96-inch-giao-tiep-i2c-white-9w56-1.jpg', 'LCD, OLED, LED Matrix, TFT displays'),
('Motor & Driver', 'motor-driver', '/images/dong-co-servo-sg90-180-do-rk7a-1.jpg', 'Động cơ và mạch điều khiển động cơ');

-- Sản phẩm Arduino & Modules
INSERT INTO products (name, slug, avg_rating, sold_quantity) VALUES
('Arduino Uno R3', 'arduino-uno-r3', 4.8, 1250),
('Arduino Nano V3', 'arduino-nano-v3', 4.7, 980),
('Arduino Mega 2560', 'arduino-mega-2560', 4.9, 450),
('ESP32 DevKit V1', 'esp32-devkit-v1', 4.9, 2100),
('ESP8266 NodeMCU', 'esp8266-nodemcu', 4.6, 1580);

-- Sản phẩm Raspberry Pi
INSERT INTO products (name, slug, avg_rating, sold_quantity) VALUES
('Raspberry Pi 4 Model B 4GB', 'raspberry-pi-4-4gb', 4.9, 850),
('Raspberry Pi 3 Model B+', 'raspberry-pi-3-b-plus', 4.8, 620),
('Raspberry Pi Zero W', 'raspberry-pi-zero-w', 4.7, 340);

-- Sản phẩm Cảm biến
INSERT INTO products (name, slug, avg_rating, sold_quantity) VALUES
('DHT22 - Cảm biến nhiệt độ và độ ẩm', 'dht22-sensor', 4.7, 1850),
('HC-SR04 - Cảm biến siêu âm', 'hc-sr04-ultrasonic', 4.6, 2230),
('BMP280 - Cảm biến áp suất và nhiệt độ', 'bmp280-pressure', 4.8, 670),
('MPU6050 - Cảm biến gia tốc và con quay hồi chuyển', 'mpu6050-imu', 4.8, 1120),
('MQ-2 - Cảm biến khí gas', 'mq2-gas-sensor', 4.5, 890);

-- Sản phẩm Module nguồn
INSERT INTO products (name, slug, avg_rating, sold_quantity) VALUES
('Module Buck LM2596 DC-DC', 'lm2596-buck-converter', 4.7, 1560),
('Module Boost MT3608', 'mt3608-boost-converter', 4.6, 1230),
('TP4056 - Module sạc pin Lithium', 'tp4056-charger', 4.8, 2450),
('AMS1117-3.3V - Module nguồn 3.3V', 'ams1117-3v3-regulator', 4.5, 890);

-- Sản phẩm Module truyền thông
INSERT INTO products (name, slug, avg_rating, sold_quantity) VALUES
('NRF24L01+ - Module RF 2.4GHz', 'nrf24l01-rf-module', 4.6, 1340),
('HC-05 - Module Bluetooth', 'hc05-bluetooth', 4.7, 1780),
('SIM800L - Module GSM/GPRS', 'sim800l-gsm', 4.4, 420),
('LoRa SX1278 433MHz', 'lora-sx1278-433mhz', 4.8, 560);

-- Sản phẩm Màn hình
INSERT INTO products (name, slug, avg_rating, sold_quantity) VALUES
('LCD 16x2 I2C xanh lá', 'lcd-16x2-i2c-green', 4.7, 1890),
('OLED 0.96" I2C 128x64', 'oled-096-i2c-128x64', 4.9, 2340),
('TFT LCD 2.4" SPI cảm ứng', 'tft-24-spi-touch', 4.6, 780);

-- Sản phẩm Motor & Driver
INSERT INTO products (name, slug, avg_rating, sold_quantity) VALUES
('L298N - Driver động cơ DC', 'l298n-motor-driver', 4.7, 1450),
('Động cơ Servo SG90', 'servo-sg90', 4.6, 2120),
('Động cơ giảm tốc 12V', 'dc-geared-motor-12v', 4.8, 670),
('Động cơ Stepper 28BYJ-48', 'stepper-28byj48', 4.5, 890);

-- Liên kết sản phẩm với danh mục
INSERT INTO product_categories (product_id, category_id) VALUES
-- Arduino & Modules
(1, 1), (2, 1), (3, 1), (4, 1), (5, 1),
-- Raspberry Pi
(6, 2), (7, 2), (8, 2),
-- Cảm biến
(9, 4), (10, 4), (11, 4), (12, 4), (13, 4),
-- Module nguồn
(14, 5), (15, 5), (16, 5), (17, 5),
-- Module truyền thông
(18, 6), (19, 6), (20, 6), (21, 6),
-- Màn hình
(22, 7), (23, 7), (24, 7),
-- Motor & Driver
(25, 8), (26, 8), (27, 8), (28, 8);

-- phân loại sản phẩm (Product Variants)
INSERT INTO product_variants (product_id, variant_name, description, specifications, price, discount_price, in_stock) VALUES
-- Arduino Uno R3
(1, 'Arduino Uno R3 - Chính hãng', 'Bo mạch Arduino Uno R3 chính hãng với chip ATmega328P, 14 digital I/O pins, 6 analog inputs, USB connection. Phù hợp cho người mới bắt đầu học lập trình nhúng.', 'Vi điều khiển: ATmega328P | Điện áp: 5V | Digital I/O: 14 (6 PWM) | Analog Input: 6 | Flash: 32KB | SRAM: 2KB | Clock: 16MHz', 180000, 165000, 45),
(1, 'Arduino Uno R3 - Compatible', 'Bo mạch Arduino Uno R3 tương thích, giá rẻ hơn, phù hợp cho học tập và thử nghiệm.', 'Vi điều khiển: ATmega328P | Điện áp: 5V | Digital I/O: 14 (6 PWM) | Analog Input: 6 | Flash: 32KB | SRAM: 2KB | Clock: 16MHz', 120000, 110000, 120),
-- Arduino Nano
(2, 'Arduino Nano V3 CH340', 'Arduino Nano V3 sử dụng chip CH340, kích thước nhỏ gọn phù hợp cho các dự án có không gian hạn chế.', 'Vi điều khiển: ATmega328P | USB: CH340 | Điện áp: 5V | Digital I/O: 14 | Analog Input: 8 | Flash: 32KB', 65000, 59000, 200),
(2, 'Arduino Nano V3 FTDI', 'Arduino Nano V3 sử dụng chip FTDI chính hãng, ổn định hơn trong giao tiếp serial.', 'Vi điều khiển: ATmega328P | USB: FTDI | Điện áp: 5V | Digital I/O: 14 | Analog Input: 8 | Flash: 32KB', 85000, NULL, 80),
-- Arduino Mega
(3, 'Arduino Mega 2560 R3', 'Arduino Mega 2560 dựa trên chip ATmega2560, có 54 digital I/O pins, 16 analog inputs, 4 UART. Lý tưởng cho các dự án phức tạp cần nhiều chân I/O.', 'Vi điều khiển: ATmega2560 | Digital I/O: 54 (15 PWM) | Analog Input: 16 | UART: 4 | Flash: 256KB | SRAM: 8KB | Clock: 16MHz', 280000, 259000, 35),
-- ESP32
(4, 'ESP32 DevKit V1 - 30 pins', 'ESP32 DevKit V1 phiên bản 30 chân, WiFi + Bluetooth tích hợp, dual-core 240MHz. Hoàn hảo cho IoT projects.', 'Chip: ESP32-WROOM-32 | CPU: Dual-core 240MHz | SRAM: 520KB | Flash: 4MB | WiFi: 802.11 b/g/n | Bluetooth: v4.2 | GPIO: 30 pins', 95000, 89000, 180),
(4, 'ESP32 DevKit V1 - 38 pins', 'ESP32 DevKit V1 phiên bản 38 chân, nhiều GPIO hơn cho các dự án phức tạp.', 'Chip: ESP32-WROOM-32 | CPU: Dual-core 240MHz | SRAM: 520KB | Flash: 4MB | WiFi: 802.11 b/g/n | Bluetooth: v4.2 | GPIO: 38 pins', 105000, 98000, 150),
-- ESP8266
(5, 'NodeMCU ESP8266 V3', 'NodeMCU ESP8266 là board phát triển WiFi giá rẻ, dễ lập trình với Arduino IDE hoặc Lua. Phù hợp cho các dự án IoT đơn giản.', 'Chip: ESP8266 | CPU: 80/160MHz | Flash: 4MB | WiFi: 802.11 b/g/n | GPIO: 17 | ADC: 1 (10-bit) | Điện áp: 3.3V', 75000, 69000, 250),
-- Raspberry Pi 4 4GB
(6, 'Raspberry Pi 4B 4GB - Kit đầy đủ', 'Kit đầy đủ bao gồm board Raspberry Pi 4B 4GB RAM, nguồn, vỏ case, thẻ nhớ, quạt tản nhiệt.', 'CPU: Quad-core ARM Cortex-A72 1.5GHz | RAM: 4GB | Display: Dual 4K | Ethernet: Gigabit | USB: 2x USB 3.0, 2x USB 2.0 | WiFi: 5GHz | Bluetooth: 5.0', 1850000, 1750000, 25),
(6, 'Raspberry Pi 4B 4GB - Board only', 'Board Raspberry Pi 4 Model B 4GB RAM, không bao gồm phụ kiện.', 'CPU: Quad-core ARM Cortex-A72 1.5GHz | RAM: 4GB | Display: Dual 4K | Ethernet: Gigabit | USB: 2x USB 3.0, 2x USB 2.0 | WiFi: 5GHz | Bluetooth: 5.0', 1450000, 1380000, 40),
-- Raspberry Pi 3 B+
(7, 'Raspberry Pi 3B+ - Kit cơ bản', 'Kit cơ bản bao gồm board Raspberry Pi 3 B+, nguồn và vỏ case.', 'CPU: Quad-core 1.4GHz | RAM: 1GB | WiFi: Dual-band | Bluetooth: 4.2 | Ethernet: Gigabit (qua USB 2.0)', 1250000, 1180000, 30),
(7, 'Raspberry Pi 3B+ - Board only', 'Board Raspberry Pi 3 Model B+ với WiFi dual-band, không phụ kiện.', 'CPU: Quad-core 1.4GHz | RAM: 1GB | WiFi: Dual-band | Bluetooth: 4.2 | Ethernet: Gigabit (qua USB 2.0)', 980000, NULL, 45),
-- Raspberry Pi Zero W
(8, 'Raspberry Pi Zero W', 'Raspberry Pi Zero W kích thước cực nhỏ với WiFi và Bluetooth tích hợp. Lý tưởng cho các dự án yêu cầu kích thước nhỏ.', 'CPU: 1GHz single-core | RAM: 512MB | WiFi: 802.11n | Bluetooth: 4.1 | GPIO: 40 pins | Video: Mini HDMI', 320000, 299000, 60),
-- DHT22
(9, 'DHT22 - Module có board', 'DHT22 module có board mạch, dễ kết nối, đo nhiệt độ và độ ẩm chính xác cao.', 'Nhiệt độ: -40~80°C (±0.5°C) | Độ ẩm: 0-100% RH (±2-5%) | Giao tiếp: Digital | Điện áp: 3.3-5V', 85000, 79000, 150),
(9, 'DHT22 - Sensor only', 'DHT22 cảm biến rời, cần tự kết nối với điện trở pull-up.', 'Nhiệt độ: -40~80°C (±0.5°C) | Độ ẩm: 0-100% RH (±2-5%) | Giao tiếp: Digital | Điện áp: 3.3-5V', 65000, NULL, 200),
-- HC-SR04
(10, 'HC-SR04 Ultrasonic', 'HC-SR04 sử dụng sóng siêu âm để đo khoảng cách từ 2cm đến 400cm với độ chính xác 3mm.', 'Khoảng cách: 2-400cm | Độ chính xác: 3mm | Tần số: 40KHz | Điện áp: 5V | Góc đo: 15°', 35000, 32000, 300),
-- BMP280
(11, 'BMP280 Module I2C', 'BMP280 đo áp suất khí quyển và nhiệt độ, giao tiếp I2C, tiêu thụ điện năng cực thấp.', 'Áp suất: 300-1100 hPa | Nhiệt độ: -40~+85°C | Giao tiếp: I2C/SPI | Điện áp: 1.8-3.6V | Độ phân giải: 0.01 hPa', 55000, 49000, 100),
-- MPU6050
(12, 'MPU6050 GY-521', 'MPU6050 tích hợp 3-axis gyroscope và 3-axis accelerometer, giao tiếp I2C. Sử dụng trong drone, robot tự cân bằng.', 'Gyroscope: 3-axis (±250/500/1000/2000°/s) | Accelerometer: 3-axis (±2/4/8/16g) | Giao tiếp: I2C | Điện áp: 3-5V | ADC: 16-bit', 65000, 59000, 180),
-- MQ-2
(13, 'MQ-2 Gas Sensor Module', 'MQ-2 phát hiện các loại khí dễ cháy như LPG, propane, methane, hydrogen. Đầu ra analog và digital.', 'Khí phát hiện: LPG, propane, methane, H2, alcohol | Đầu ra: Analog + Digital | Điện áp: 5V | Thời gian đáp ứng: <10s', 45000, 39000, 120),
-- LM2596 Buck
(14, 'LM2596 Buck Converter', 'LM2596 step-down converter với dòng 3A, điện áp đầu vào 4-40V, đầu ra điều chỉnh 1.25-37V.', 'Dòng ra: 3A | Vin: 4-40V | Vout: 1.25-37V | Hiệu suất: 92% | Tần số: 150KHz', 35000, 32000, 200),
-- MT3608 Boost
(15, 'MT3608 Boost Module', 'MT3608 step-up converter tăng điện áp từ 2-24V lên 5-28V với dòng 2A.', 'Dòng ra: 2A | Vin: 2-24V | Vout: 5-28V | Hiệu suất: 93% | Tần số: 1.2MHz', 25000, 22000, 180),
-- TP4056
(16, 'TP4056 với bảo vệ', 'TP4056 sạc pin lithium 1S với dòng sạc 1A, có mạch bảo vệ quá sạc, quá phóng, ngắn mạch.', 'Dòng sạc: 1A | Vin: 5V (Micro USB) | Pin: Li-ion/Li-Po 1S | Bảo vệ: Quá sạc, quá phóng, ngắn mạch', 18000, 15000, 400),
(16, 'TP4056 không bảo vệ', 'TP4056 sạc pin lithium 1S với dòng sạc 1A, không có mạch bảo vệ.', 'Dòng sạc: 1A | Vin: 5V (Micro USB) | Pin: Li-ion/Li-Po 1S | Bảo vệ: Không', 12000, NULL, 350),
-- AMS1117
(17, 'AMS1117-3.3V Module', 'AMS1117-3.3V cung cấp nguồn ổn định 3.3V với dòng tối đa 1A.', 'Vout: 3.3V | Dòng ra: 1A | Vin: 4.5-12V | Loại: Linear regulator | Dropout: 1.3V', 15000, 12000, 250),
-- NRF24L01+
(18, 'NRF24L01+ cơ bản', 'NRF24L01+ module RF cơ bản, anten tích hợp trên PCB, khoảng cách ~100m.', 'Tần số: 2.4GHz | Tốc độ: 250Kbps-2Mbps | Khoảng cách: ~100m | Giao tiếp: SPI | Điện áp: 3.3V', 35000, 32000, 150),
(18, 'NRF24L01+ có anten', 'NRF24L01+ module RF với anten ngoài, khoảng cách truyền xa hơn ~1000m.', 'Tần số: 2.4GHz | Tốc độ: 250Kbps-2Mbps | Khoảng cách: ~1000m | Giao tiếp: SPI | Điện áp: 3.3V | Anten: Ngoài', 55000, 49000, 100),
-- HC-05
(19, 'HC-05 Master/Slave', 'HC-05 là module Bluetooth 2.0 có thể cấu hình Master/Slave, giao tiếp UART đơn giản.', 'Bluetooth: 2.0 | Baud rate: 9600-115200 | Khoảng cách: 10m | Giao tiếp: UART | Điện áp: 3.3-6V | Chế độ: Master/Slave', 95000, 89000, 200),
-- SIM800L
(20, 'SIM800L GSM Module', 'SIM800L hỗ trợ GSM/GPRS quad-band, gọi điện thoại, gửi/nhận SMS, GPRS data.', 'Mạng: GSM/GPRS Quad-band | Giao tiếp: UART | SIM: Mini SIM | Điện áp: 3.4-4.4V | Dòng: 2A peak', 180000, 165000, 60),
-- LoRa SX1278
(21, 'LoRa 433MHz SX1278', 'LoRa SX1278 hoạt động ở tần số 433MHz, khoảng cách truyền 3-5km, tiêu thụ điện năng cực thấp.', 'Tần số: 433MHz | Khoảng cách: 3-5km | Tốc độ: 0.3-19.2Kbps | Giao tiếp: SPI | Điện áp: 3.3V | Công suất: 20dBm', 135000, 125000, 80),
-- LCD 16x2
(22, 'LCD 1602 I2C xanh lá', 'LCD 1602 nền xanh lá chữ trắng với module I2C, chỉ cần 2 chân SDA/SCL.', 'Hiển thị: 16x2 ký tự | Giao tiếp: I2C | Nền: Xanh lá | Chữ: Trắng | Điện áp: 5V | Điều chỉnh độ tương phản: Có', 65000, 59000, 180),
(22, 'LCD 1602 I2C xanh dương', 'LCD 1602 nền xanh dương chữ trắng với module I2C, chỉ cần 2 chân SDA/SCL.', 'Hiển thị: 16x2 ký tự | Giao tiếp: I2C | Nền: Xanh dương | Chữ: Trắng | Điện áp: 5V | Điều chỉnh độ tương phản: Có', 65000, 59000, 150),
-- OLED
(23, 'OLED 0.96" màu trắng', 'OLED 0.96 inch màu trắng, độ phân giải 128x64 pixels, góc nhìn rộng, độ tương phản cao.', 'Kích thước: 0.96 inch | Phân giải: 128x64 | Màu: Trắng | Giao tiếp: I2C | Điện áp: 3.3-5V | Driver: SSD1306', 85000, 79000, 250),
(23, 'OLED 0.96" màu xanh', 'OLED 0.96 inch màu xanh dương, độ phân giải 128x64 pixels, góc nhìn rộng.', 'Kích thước: 0.96 inch | Phân giải: 128x64 | Màu: Xanh dương | Giao tiếp: I2C | Điện áp: 3.3-5V | Driver: SSD1306', 85000, 79000, 200),
-- TFT 2.4"
(24, 'TFT 2.4" cảm ứng', 'TFT 2.4 inch màu 262K với độ phân giải 240x320, tích hợp cảm ứng điện trở.', 'Kích thước: 2.4 inch | Phân giải: 240x320 | Màu: 262K | Cảm ứng: Điện trở | Giao tiếp: SPI | Driver: ILI9341', 185000, 169000, 70),
-- L298N
(25, 'L298N Motor Driver', 'L298N có thể điều khiển 2 động cơ DC hoặc 1 động cơ stepper, dòng tối đa 2A/motor.', 'Dòng: 2A/motor | Điện áp motor: 5-35V | Điều khiển: 2 DC hoặc 1 Stepper | Ổn áp 5V tích hợp: Có | Logic: 5V', 55000, 49000, 180),
-- Servo SG90
(26, 'Servo SG90 9g', 'SG90 là servo nhỏ gọn trọng lượng 9g, góc quay 180°, điều khiển PWM.', 'Trọng lượng: 9g | Góc quay: 180° | Momen xoắn: 1.8kg.cm | Điều khiển: PWM | Điện áp: 4.8-6V | Tốc độ: 0.1s/60°', 35000, 32000, 300),
-- DC Motor
(27, 'DC Motor 12V 100RPM', 'Động cơ DC 12V với hộp số giảm tốc tích hợp, tốc độ 100RPM, có encoder đo tốc độ.', 'Điện áp: 12V | Tốc độ: 100RPM | Encoder: Có | Momen xoắn: Cao | Trục: 6mm | Ứng dụng: Robot di động', 125000, 115000, 90),
-- Stepper
(28, 'Stepper 28BYJ-48 + ULN2003', '28BYJ-48 là động cơ bước 5V giảm tốc 1/64, đi kèm driver ULN2003.', 'Điện áp: 5V | Giảm tốc: 1/64 | Góc bước: 5.625° | Driver: ULN2003 | Số pha: 4 | Tiêu thụ: ~240mA', 65000, 59000, 120);

-- Đánh giá sản phẩm
INSERT INTO reviews (user_id, product_id, rating, comment) VALUES
(2, 1, 5, 'Arduino Uno chất lượng tốt, ship nhanh, đóng gói cẩn thận. Rất hài lòng!'),
(3, 1, 5, 'Sản phẩm chính hãng, hoạt động ổn định. Shop tư vấn nhiệt tình.'),
(2, 4, 5, 'ESP32 rất mạnh, WiFi ổn định, lập trình dễ dàng. Giá cả phải chăng.'),
(3, 9, 4, 'DHT22 đo chính xác, giao tiếp đơn giản. Đóng gói tốt.'),
(2, 10, 5, 'HC-SR04 hoạt động tốt, đo khoảng cách chính xác. Giá rẻ.'),
(3, 23, 5, 'OLED sắc nét, độ tương phản cao, rất đẹp. Recommend!'),
(2, 19, 4, 'HC-05 kết nối ổn định, dễ cấu hình. Tốt với giá tiền này.');