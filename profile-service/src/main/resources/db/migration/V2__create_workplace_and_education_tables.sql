-- V2__create_workplace_and_education_tables.sql

-- 1. Bảng nơi làm việc
CREATE TABLE user_workplaces
(
    id            VARCHAR(64)  PRIMARY KEY,
    user_id       VARCHAR(64)  NOT NULL,
    company_name  VARCHAR(255) NOT NULL,
    position      VARCHAR(255),
    description   TEXT,
    start_date    DATE,
    end_date      DATE,
    is_current    BOOLEAN      DEFAULT FALSE,
    privacy       VARCHAR(20)  DEFAULT 'PUBLIC',
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workplaces_info FOREIGN KEY (user_id) REFERENCES user_infos (user_id) ON DELETE CASCADE
);

CREATE INDEX idx_workplaces_userid ON user_workplaces (user_id);

CREATE TABLE user_educations
(
    id           VARCHAR(64) PRIMARY KEY,
    user_id      VARCHAR(64)  NOT NULL,
    school_name  VARCHAR(255) NOT NULL,
    type         VARCHAR(50),
    major        VARCHAR(255),
    degree       VARCHAR(255),
    start_date   DATE,
    end_date     DATE,
    is_graduated BOOLEAN      DEFAULT FALSE,
    privacy      VARCHAR(20)  DEFAULT 'PUBLIC',
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_educations_info FOREIGN KEY (user_id) REFERENCES user_infos (user_id) ON DELETE CASCADE
);

CREATE INDEX idx_educations_userid ON user_educations (user_id);