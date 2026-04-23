CREATE TABLE user_devices (
                              id VARCHAR(64) PRIMARY KEY,
                              user_id VARCHAR(64) NOT NULL,
                              refresh_token_id VARCHAR(64),
                              device_name VARCHAR(255),
                              device_type VARCHAR(100),
                              ip_address VARCHAR(45),
                              location TEXT,
                              last_active TIMESTAMP,
                              created_at TIMESTAMP,
                              CONSTRAINT fk_device_token FOREIGN KEY (refresh_token_id) REFERENCES refresh_tokens(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_device_user_id ON user_devices(user_id);