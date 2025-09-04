CREATE TABLE user (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(200) NOT NULL,
                       password_hash VARCHAR(200) NOT NULL,
                       nickname VARCHAR(100) NOT NULL,
                       region VARCHAR(100),
                       profile_image_url VARCHAR(500),
                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                       gender VARCHAR(10),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);