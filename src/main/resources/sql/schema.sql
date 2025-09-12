DROP TABLE IF EXISTS users;
CREATE TABLE IF NOT EXISTS users  (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(200) NOT NULL UNIQUE,
    password_hash VARCHAR(200) NOT NULL,
    nickname VARCHAR(100) NOT NULL UNIQUE,
    region VARCHAR(100),
    profile_image_url VARCHAR(500),
    gender VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_interests (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id  BIGINT NOT NULL,
    category VARCHAR(30) NOT NULL,
    CONSTRAINT uk_user_category UNIQUE (user_id, category),
    CONSTRAINT fk_user_interest_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE
);

DROP TABLE IF EXISTS email_verifications;
CREATE TABLE IF NOT EXISTS email_verifications (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(200) NOT NULL,
    code  VARCHAR(6)   NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_email_verifications_email
    ON email_verifications (email);
