-- =====================================================
-- BẢNG CHO PHÂN HỆ LỊCH SỬ TÌM KIẾM & XU HƯỚNG TỪ KHÓA
-- =====================================================

CREATE TABLE IF NOT EXISTS search_histories (
    id BIGSERIAL PRIMARY KEY,
    keyword VARCHAR(255) NOT NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    results_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_search_histories_keyword ON search_histories(keyword);
CREATE INDEX IF NOT EXISTS idx_search_histories_created_at ON search_histories(created_at DESC);
