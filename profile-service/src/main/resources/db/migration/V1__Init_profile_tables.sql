-- ==========================================
-- 2. BẢNG USER_INFOS (Quản lý Thông tin)
-- ==========================================
CREATE TABLE user_infos
(
    user_id             VARCHAR(64) PRIMARY KEY, -- PK là userId từ Identity
    username            VARCHAR(255) NOT NULL,
    avatar_url          VARCHAR(500),
    cover_url           VARCHAR(500),
    bio                 TEXT,
    location            VARCHAR(255),
    birth_date          DATE,
    website             VARCHAR(255),
    relationship_status VARCHAR(50),
    hometown            VARCHAR(255),
    current_city        VARCHAR(255),
    privacy             VARCHAR(20) DEFAULT 'PUBLIC', -- PUBLIC, PRIVATE, FRIENDS

    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
