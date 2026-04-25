CREATE TABLE user_credential (
                                 id VARCHAR(64) PRIMARY KEY,

                                 email VARCHAR(255) UNIQUE NOT NULL,
                                 password VARCHAR(255) NOT NULL,
                                 is_verified BOOLEAN DEFAULT FALSE,
                                 status INT DEFAULT 1,
                                 verified_at TIMESTAMP,
                                 created_at TIMESTAMP,
                                 updated_at TIMESTAMP
);
CREATE INDEX idx_user_email ON user_credential(email);