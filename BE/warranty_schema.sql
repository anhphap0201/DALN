-- =====================================================
-- BẢNG CHO PHÂN HỆ QUẢN LÝ BẢO HÀNH (WARRANTY MANAGEMENT)
-- =====================================================

-- 1. Bổ sung cột warranty_months vào bảng products nếu chưa có
ALTER TABLE products ADD COLUMN IF NOT EXISTS warranty_months INTEGER DEFAULT 12;

-- Cập nhật mặc định 12 tháng cho các sản phẩm hiện có
UPDATE products SET warranty_months = 12 WHERE warranty_months IS NULL;

-- 2. Bảng quản lý Mã tem / Serial Number của linh kiện xuất kho
CREATE TABLE IF NOT EXISTS product_serials (
    id BIGSERIAL PRIMARY KEY,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    product_variant_id BIGINT REFERENCES product_variants(id) ON DELETE SET NULL,
    order_item_id BIGINT REFERENCES order_items(id) ON DELETE SET NULL,
    order_code VARCHAR(100),
    customer_phone VARCHAR(50),
    customer_name VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'EXPIRED', 'WARRANTY_SERVICING'
    activated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_product_serials_serial ON product_serials(serial_number);
CREATE INDEX IF NOT EXISTS idx_product_serials_order ON product_serials(order_code);
CREATE INDEX IF NOT EXISTS idx_product_serials_phone ON product_serials(customer_phone);

-- 3. Bảng yêu cầu / Phiếu bảo hành (Warranty Claims)
CREATE TABLE IF NOT EXISTS warranty_claims (
    id BIGSERIAL PRIMARY KEY,
    claim_code VARCHAR(100) NOT NULL UNIQUE,
    serial_id BIGINT REFERENCES product_serials(id) ON DELETE CASCADE,
    serial_number VARCHAR(100) NOT NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(50) NOT NULL,
    customer_email VARCHAR(255),
    issue_description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'PROCESSING', 'COMPLETED', 'REJECTED'
    resolution_type VARCHAR(50), -- 'REPAIRED', 'REPLACED_NEW', 'REJECTED'
    technician_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_warranty_claims_code ON warranty_claims(claim_code);
CREATE INDEX IF NOT EXISTS idx_warranty_claims_serial ON warranty_claims(serial_number);
CREATE INDEX IF NOT EXISTS idx_warranty_claims_phone ON warranty_claims(customer_phone);
CREATE INDEX IF NOT EXISTS idx_warranty_claims_status ON warranty_claims(status);
