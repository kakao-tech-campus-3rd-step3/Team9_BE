CREATE TABLE user (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(200) NOT NULL UNIQUE,
                       password_hash VARCHAR(200) NOT NULL,
                       nickname VARCHAR(100) NOT NULL UNIQUE,
                       region VARCHAR(100),
                       profile_image_url VARCHAR(500),
                       gender ENUM('MALE', 'FEMALE') NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);