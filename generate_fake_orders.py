#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script sinh đơn hàng ảo (Mock Orders Generator) cho dự án E-commerce Electronic Components.
Dữ liệu sinh ra tương thích hoàn toàn với PostgreSQL và cấu trúc Entity của Spring Boot backend.
"""

import sys
import random
import argparse
from datetime import datetime, timedelta
from decimal import Decimal
import psycopg2
from psycopg2.extras import execute_values

# Cấu hình kết nối Database mặc định (theo application.yaml)
DB_CONFIG = {
    "dbname": "test_api",
    "user": "postgres",
    "password": "123321",
    "host": "localhost",
    "port": 5432
}

# Họ và tên mẫu Việt Nam
FIRST_NAMES = ["Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ", "Võ", "Đặng", "Bùi", "Đỗ", "Hồ", "Ngô", "Dương", "Lý"]
MIDDLE_NAMES = ["Văn", "Thị", "Hữu", "Đức", "Minh", "Thanh", "Anh", "Quang", "Gia", "Bảo", "Tuấn", "Hoàng", "Xuân", "Hải"]
LAST_NAMES = ["Pháp", "Nam", "Hưng", "Dũng", "Khánh", "Long", "Tùng", "Khoa", "Phong", "Thịnh", "Thắng", "Quân", "Đạt", "Linh", "Trang", "Hương", "Hà", "Vy", "Mai", "Nhi", "Yến"]

# Tỉnh thành và quận huyện mẫu
LOCATIONS = [
    {"province": "Hà Nội", "code": "HN", "ward": "Phường Bách Khoa, Quận Hai Bà Trưng", "ward_code": "HN01"},
    {"province": "Hà Nội", "code": "HN", "ward": "Phường Dịch Vọng, Quận Cầu Giấy", "ward_code": "HN02"},
    {"province": "TP. Hồ Chí Minh", "code": "HCM", "ward": "Phường Bến Nghé, Quận 1", "ward_code": "HCM01"},
    {"province": "TP. Hồ Chí Minh", "code": "HCM", "ward": "Phường Linh Trung, TP. Thủ Đức", "ward_code": "HCM02"},
    {"province": "Đà Nẵng", "code": "DN", "ward": "Phường Hải Châu 1, Quận Hải Châu", "ward_code": "DN01"},
    {"province": "Cần Thơ", "code": "CT", "ward": "Phường Xuân Khánh, Quận Ninh Kiều", "ward_code": "CT01"},
    {"province": "Hải Phòng", "code": "HP", "ward": "Phường Máy Tơ, Quận Ngô Quyền", "ward_code": "HP01"},
    {"province": "Bình Dương", "code": "BD", "ward": "Phường Phú Hòa, TP. Thủ Dầu Một", "ward_code": "BD01"},
]

PAYMENT_METHODS = ["COD", "VNPAY", "PAYOS"]
SHIPPING_METHODS = ["STANDARD", "EXPRESS", "SMART_LOCKER"]

# Tỉ lệ phân bố trạng thái đơn hàng thực tế
STATUS_CHOICES = ["DELIVERED"] * 65 + ["SHIPPED"] * 15 + ["CONFIRMED"] * 10 + ["PENDING"] * 5 + ["CANCELLED"] * 5

def generate_vietnamese_name():
    first = random.choice(FIRST_NAMES)
    middle = random.choice(MIDDLE_NAMES)
    last = random.choice(LAST_NAMES)
    return f"{first} {middle} {last}"

def generate_phone():
    prefix = random.choice(["09", "03", "07", "08"])
    suffix = "".join([str(random.randint(0, 9)) for _ in range(8)])
    return f"{prefix}{suffix}"

def generate_email(full_name, order_idx):
    # Tạo email không dấu từ tên
    name_clean = full_name.lower().replace(" ", "")
    # loại bỏ dấu tiếng Việt cơ bản cho email
    trans = str.maketrans("àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ",
                          "aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd")
    ascii_name = name_clean.translate(trans)
    domains = ["gmail.com", "yahoo.com", "outlook.com", "icloud.com"]
    return f"{ascii_name}{random.randint(10, 999)}@{random.choice(domains)}"

def main():
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")

    parser = argparse.ArgumentParser(description="Sinh đơn hàng giả lập cho Dashboard DALN")
    parser.add_argument("--count", type=int, default=75, help="Số lượng đơn hàng cần sinh (mặc định: 75)")
    parser.add_argument("--days", type=int, default=30, help="Phạm vi số ngày trong quá khứ (mặc định: 30 ngày)")
    parser.add_argument("--clear-mock", action="store_true", help="Xóa các đơn hàng ảo (có tiền tố MOCK-) trước khi sinh mới")
    parser.add_argument("--host", default=DB_CONFIG["host"], help="PostgreSQL host")
    parser.add_argument("--port", type=int, default=DB_CONFIG["port"], help="PostgreSQL port")
    parser.add_argument("--dbname", default=DB_CONFIG["dbname"], help="Database name")
    parser.add_argument("--user", default=DB_CONFIG["user"], help="PostgreSQL username")
    parser.add_argument("--password", default=DB_CONFIG["password"], help="PostgreSQL password")

    args = parser.parse_args()

    print("==================================================")
    print("   E-COMMERCE MOCK ORDERS GENERATOR (DALN)")
    print("==================================================")
    print(f"Connecting to: {args.user}@{args.host}:{args.port}/{args.dbname}")

    try:
        conn = psycopg2.connect(
            dbname=args.dbname,
            user=args.user,
            password=args.password,
            host=args.host,
            port=args.port
        )
        conn.autocommit = False
        cur = conn.cursor()
    except Exception as e:
        print(f"[ERROR] Không thể kết nối tới cơ sở dữ liệu: {e}")
        return

    try:
        # Nếu có cờ xóa đơn ảo cũ
        if args.clear_mock:
            print("\n>> Đang dọn dẹp các đơn hàng ảo cũ có tiền tố 'MOCK-'...")
            cur.execute("DELETE FROM orders WHERE order_number LIKE 'MOCK-%'")
            deleted_count = cur.rowcount
            conn.commit()
            print(f">> Đã xóa thành công {deleted_count} đơn hàng ảo cũ.")

        # 1. Lấy danh sách Users
        cur.execute("SELECT id FROM users")
        user_rows = cur.fetchall()
        user_ids = [r[0] for r in user_rows] if user_rows else [None]

        # 2. Lấy danh sách Product Variants
        cur.execute("""
            SELECT pv.id, pv.product_id, pv.price, pv.variant_name, p.name 
            FROM product_variants pv 
            JOIN products p ON pv.product_id = p.id
            WHERE pv.is_available = true OR pv.is_available IS NULL
        """)
        variants = cur.fetchall()

        if not variants:
            print("[ERROR] Không tìm thấy product variants nào trong CSDL! Hãy đảm bảo bảng products và product_variants có dữ liệu.")
            conn.close()
            return

        print(f">> Tìm thấy {len(variants)} biến thể sản phẩm và {len(user_ids)} người dùng hợp lệ.")
        print(f">> Chuẩn bị tạo {args.count} đơn hàng ngẫu nhiên trong vòng {args.days} ngày qua...")

        now = datetime.now()
        created_orders = 0
        total_revenue_generated = Decimal("0")
        sold_quantities = {} # product_id -> total_sold

        for i in range(1, args.count + 1):
            # Tính thời gian ngẫu nhiên trong khoảng args.days ngày
            days_ago = random.uniform(0, args.days)
            order_time = now - timedelta(days=days_ago)

            status = random.choice(STATUS_CHOICES)
            payment_method = random.choice(PAYMENT_METHODS)
            shipping_method = random.choice(SHIPPING_METHODS)
            loc = random.choice(LOCATIONS)
            full_name = generate_vietnamese_name()
            phone = generate_phone()
            email = generate_email(full_name, i)
            user_id = random.choice(user_ids)

            # Chọn từ 1 đến 4 biến thể sản phẩm cho đơn
            num_items = random.choices([1, 2, 3, 4], weights=[45, 35, 15, 5])[0]
            chosen_variants = random.sample(variants, min(num_items, len(variants)))

            order_items_data = []
            subtotal = Decimal("0")

            for var in chosen_variants:
                var_id, prod_id, var_price, var_name, prod_name = var
                qty = random.randint(1, 4)
                price = Decimal(str(var_price))
                item_total = price * qty
                subtotal += item_total
                order_items_data.append((var_id, prod_id, qty, price))

                if status == "DELIVERED":
                    sold_quantities[prod_id] = sold_quantities.get(prod_id, 0) + qty

            shipping_fee = Decimal(random.choice(["25000", "30000", "35000", "40000"]))
            discount = Decimal(random.choice(["0", "0", "0", "15000", "20000", "30000"])) if subtotal > 150000 else Decimal("0")
            total_cost = max(Decimal("0"), subtotal + shipping_fee - discount)

            if status == "DELIVERED":
                total_revenue_generated += total_cost

            # Thiết lập các mốc thời gian phụ thuộc status
            confirmed_at = order_time + timedelta(minutes=random.randint(15, 120)) if status in ["CONFIRMED", "SHIPPED", "DELIVERED"] else None
            shipped_at = confirmed_at + timedelta(hours=random.randint(4, 24)) if confirmed_at and status in ["SHIPPED", "DELIVERED"] else None
            delivered_at = shipped_at + timedelta(days=random.randint(1, 3)) if shipped_at and status == "DELIVERED" else None
            cancelled_at = order_time + timedelta(hours=random.randint(1, 12)) if status == "CANCELLED" else None

            # Mã đơn hàng duy nhất có tiền tố MOCK-
            timestamp_str = order_time.strftime("%y%m%d%H%M")
            order_number = f"MOCK-{timestamp_str}-{random.randint(100, 999)}"

            payment_status = "PAID" if (payment_method != "COD" or status == "DELIVERED") else "PENDING"
            address = f"Số {random.randint(1, 350)} Đường {random.choice(['Nguyễn Trãi', 'Lê Lợi', 'Trần Hưng Đạo', 'Điện Biên Phủ', 'Hoàng Hoa Thám', 'Giải Phóng'])}"

            # Chèn Order vào database
            cur.execute("""
                INSERT INTO orders (
                    user_id, status, total_cost, total_price, subtotal, total,
                    shipping_cost, shipping_fee, discount, full_name, email, phone,
                    address, province, province_code, ward, ward_code,
                    order_number, payment_method, payment_status, shipping_method,
                    note, created_at, updated_at, confirmed_at, shipped_at,
                    delivered_at, cancelled_at
                ) VALUES (
                    %s, %s, %s, %s, %s, %s,
                    %s, %s, %s, %s, %s, %s,
                    %s, %s, %s, %s, %s,
                    %s, %s, %s, %s,
                    %s, %s, %s, %s, %s,
                    %s, %s
                ) RETURNING id
            """, (
                user_id, status, total_cost, total_cost, subtotal, total_cost,
                shipping_fee, shipping_fee, discount, full_name, email, phone,
                address, loc["province"], loc["code"], loc["ward"], loc["ward_code"],
                order_number, payment_method, payment_status, shipping_method,
                "Đơn hàng tự động giả lập", order_time, delivered_at or order_time,
                confirmed_at, shipped_at, delivered_at, cancelled_at
            ))

            order_id = cur.fetchone()[0]

            # Chèn Order Items
            for item in order_items_data:
                var_id, prod_id, qty, price = item
                cur.execute("""
                    INSERT INTO order_items (
                        order_id, product_variant_id, quantity, unit_price, created_at
                    ) VALUES (%s, %s, %s, %s, %s)
                """, (order_id, var_id, qty, price, order_time))

            created_orders += 1

        # Cập nhật sold_quantity của products
        print(">> Đang cập nhật số lượng đã bán (sold_quantity) cho các sản phẩm...")
        for prod_id, added_sold in sold_quantities.items():
            cur.execute("""
                UPDATE products 
                SET sold_quantity = COALESCE(sold_quantity, 0) + %s 
                WHERE id = %s
            """, (added_sold, prod_id))

        conn.commit()
        print("\n==================================================")
        print(f" [THÀNH CÔNG] Đã tạo {created_orders} đơn hàng giả lập!")
        print(f" Tổng doanh thu giả lập hoàn thành: {total_revenue_generated:,.0f} VNĐ")
        print(f" Số lượng sản phẩm đã bán được cập nhật: {sum(sold_quantities.values())} đơn vị")
        print("==================================================")
        print("Bạn có thể reload lại trang Dashboard Admin để thấy biểu đồ cập nhật đầy đủ dữ liệu!")

    except Exception as e:
        conn.rollback()
        print(f"\n[LỖI NGHIÊM TRỌNG] Đã rollback giao dịch: {e}")
        import traceback
        traceback.print_exc()
    finally:
        cur.close()
        conn.close()

if __name__ == "__main__":
    main()
