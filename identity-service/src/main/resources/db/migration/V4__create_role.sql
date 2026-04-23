CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(50) UNIQUE NOT NULL,
                       description TEXT
);

CREATE TABLE user_roles (
                            id VARCHAR(64) PRIMARY KEY,
                            user_id VARCHAR(64) NOT NULL,
                            role_id INT NOT NULL,
                            CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES user_credential(id) ON DELETE CASCADE,
                            CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Chèn mặc định các Role cơ bản
INSERT INTO roles (name, description) VALUES ('ADMIN', 'Quản trị viên hệ thống');
INSERT INTO roles (name, description) VALUES ('USER', 'Người dùng thông thường');
ALTER TABLE user_roles ADD CONSTRAINT unique_user_role UNIQUE (user_id, role_id);