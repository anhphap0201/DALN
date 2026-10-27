-- =====================================================
-- DỮ LIỆU MẪU CHO PHÂN HỆ BẢO HÀNH (SEED DATA)
-- =====================================================

-- 1. Chèn mã tem serial bảo hành mẫu
INSERT INTO product_serials (
    serial_number, product_id, product_variant_id, order_code, customer_phone, customer_name, status, activated_at, expires_at, notes
) VALUES 
(
    'SN-202610-ARDUINO-01', 1, 1, 'ORD-202610-001', '0912345678', 'Nguyễn Văn An', 'ACTIVE',
    CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP + INTERVAL '350 days', 'Linh kiện xuất kho chính hãng nguyên seal'
),
(
    'SN-202610-ESP32-99', 4, 1, 'ORD-202610-002', '0987654321', 'Trần Thị Mai', 'WARRANTY_SERVICING',
    CURRENT_TIMESTAMP - INTERVAL '2 months', CURRENT_TIMESTAMP + INTERVAL '4 months', 'Đang tiếp nhận kiểm tra chân GPIO'
),
(
    'SN-202501-EXPIRED-01', 3, 5, 'ORD-202501-999', '0903123456', 'Lê Hoàng Nam', 'EXPIRED',
    CURRENT_TIMESTAMP - INTERVAL '15 months', CURRENT_TIMESTAMP - INTERVAL '3 months', 'Linh kiện hết hạn bảo hành 12 tháng'
)
ON CONFLICT (serial_number) DO NOTHING;

-- 2. Chèn phiếu yêu cầu bảo hành mẫu
INSERT INTO warranty_claims (
    claim_code, serial_id, serial_number, customer_name, customer_phone, customer_email, issue_description, status, resolution_type, technician_notes, created_at
) VALUES 
(
    'WR-202610-8842', 
    (SELECT id FROM product_serials WHERE serial_number = 'SN-202610-ARDUINO-01' LIMIT 1),
    'SN-202610-ARDUINO-01',
    'Nguyễn Văn An',
    '0912345678',
    'an.nguyen@gmail.com',
    'Bo mạch Arduino cắm cáp USB vào máy tính không nhận diện cổng COM (Device Manager báo Code 43)',
    'PENDING',
    NULL,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '1 day'
),
(
    'WR-202610-9911',
    (SELECT id FROM product_serials WHERE serial_number = 'SN-202610-ESP32-99' LIMIT 1),
    'SN-202610-ESP32-99',
    'Trần Thị Mai',
    '0987654321',
    'mai.tran@gmail.com',
    'Module WiFi ESP32 phát nhiệt rất nhanh và sụt áp cổng 3.3V',
    'PROCESSING',
    'REPAIRED',
    'Đã tiếp nhận sản phẩm tại phòng kỹ thuật, đang kiểm tra thay thế tụ lọc nguồn ngõ vào',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
)
ON CONFLICT (claim_code) DO NOTHING;
