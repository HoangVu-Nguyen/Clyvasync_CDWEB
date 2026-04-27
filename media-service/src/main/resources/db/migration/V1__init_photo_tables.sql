CREATE TABLE user_photos (
                             id VARCHAR(64) PRIMARY KEY,
                             user_id VARCHAR(64) NOT NULL,
                             photo_url VARCHAR(500) NOT NULL,
                             photo_type VARCHAR(50) NOT NULL,
                             privacy VARCHAR(20) DEFAULT 'PUBLIC',
                             is_current BOOLEAN DEFAULT FALSE,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_active_user_photos ON user_photos(user_id, photo_type) WHERE is_current = true;