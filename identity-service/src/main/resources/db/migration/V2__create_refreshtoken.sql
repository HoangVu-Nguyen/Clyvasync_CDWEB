CREATE TABLE refresh_tokens (
                                id VARCHAR(64) PRIMARY KEY,
                                token VARCHAR(500) NOT NULL UNIQUE,
                                email VARCHAR(255) NOT NULL,
                                expiry_date TIMESTAMP NOT NULL,
                                device_id VARCHAR(255),
                                ip_address VARCHAR(50),
                                revoked BOOLEAN DEFAULT FALSE,
                                created_at TIMESTAMP,
                                updated_at TIMESTAMP
);

CREATE INDEX idx_token ON refresh_tokens(token);
CREATE INDEX idx_email_device ON refresh_tokens(email, device_id);