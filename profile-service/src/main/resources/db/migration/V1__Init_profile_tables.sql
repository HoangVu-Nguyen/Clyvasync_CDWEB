-- ==========================================
-- 2. BẢNG USER_INFOS (Quản lý Thông tin)
-- ==========================================
CREATE TABLE user_infos
(
    id         VARCHAR(64) PRIMARY KEY,
    user_id    VARCHAR(64)  NOT NULL UNIQUE,
    username   VARCHAR(255) NOT NULL,
    bio        TEXT,
    location   VARCHAR(255),
    birth_date DATE,
    avatar_url VARCHAR(500),
    cover_url  VARCHAR(500),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

