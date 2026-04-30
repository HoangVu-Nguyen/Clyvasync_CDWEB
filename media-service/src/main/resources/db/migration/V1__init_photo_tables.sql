CREATE TABLE user_photos
(
    id         VARCHAR(64) PRIMARY KEY,
    user_id    VARCHAR(64)  NOT NULL,
    -- Lưu ObjectKey khi PENDING, lưu Full URL khi ACTIVE
    photo_url  VARCHAR(500) NOT NULL,
    -- Enum: 'AVATAR', 'COVER'
    photo_type VARCHAR(50)  NOT NULL,
    -- Enum: 'PUBLIC', 'PRIVATE'
    privacy    VARCHAR(20) DEFAULT 'PUBLIC',
    -- Enum: 'PENDING', 'ACTIVE', 'DELETED'
    status     VARCHAR(20) DEFAULT 'PENDING',
    is_current BOOLEAN     DEFAULT FALSE,
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'ACTIVE', 'DELETED')),
    CONSTRAINT chk_type CHECK (photo_type IN ('AVATAR', 'COVER'))
);

CREATE INDEX idx_active_user_photos ON user_photos (user_id, photo_type)
    WHERE is_current = true AND status = 'ACTIVE';

CREATE INDEX idx_cleanup_pending_photos ON user_photos (status, created_at)
    WHERE status = 'PENDING';