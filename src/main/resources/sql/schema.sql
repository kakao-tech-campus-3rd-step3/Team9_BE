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

DROP TABLE IF EXISTS study_member;
DROP TABLE IF EXISTS study_application;
DROP TABLE IF EXISTS study_category;
DROP TABLE IF EXISTS study_condition;
DROP TABLE IF EXISTS study;

CREATE TABLE study (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   leader_id BIGINT NOT NULL,
   title VARCHAR(100) NOT NULL,
   description VARCHAR(200) NOT NULL,
   detail_description TEXT,
   study_time VARCHAR(100),
   max_members INTEGER NOT NULL,
   file_key VARCHAR(500),
   region VARCHAR(50) NOT NULL,
   status VARCHAR(50) NOT NULL DEFAULT 'RECRUITING',
   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   CONSTRAINT fk_study_leader FOREIGN KEY (leader_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_study_status_created_at ON study (status, created_at);
CREATE INDEX idx_study_region ON study (region);

CREATE TABLE study_condition (
     study_id BIGINT NOT NULL,
     content VARCHAR(500) NOT NULL,
     PRIMARY KEY (study_id, content),
     CONSTRAINT fk_study_condition_study FOREIGN KEY (study_id) REFERENCES study (id) ON DELETE CASCADE
);

CREATE TABLE study_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    study_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    CONSTRAINT uk_study_category UNIQUE (study_id, category),
    CONSTRAINT fk_study_category_study FOREIGN KEY (study_id) REFERENCES study (id) ON DELETE CASCADE
);

CREATE INDEX idx_study_category_study_id ON study_category (study_id);
CREATE INDEX idx_study_category_category ON study_category (category);

CREATE TABLE study_application (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   study_id BIGINT NOT NULL,
   user_id BIGINT NOT NULL,
   status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
   message VARCHAR(500),
   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   CONSTRAINT uk_study_application UNIQUE (study_id, user_id),
   CONSTRAINT fk_study_application_study FOREIGN KEY (study_id) REFERENCES study (id) ON DELETE CASCADE,
   CONSTRAINT fk_study_application_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE study_member (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  study_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
  message VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_study_member_study_user UNIQUE (study_id, user_id),
  CONSTRAINT fk_study_member_study FOREIGN KEY (study_id) REFERENCES study (id) ON DELETE CASCADE,
  CONSTRAINT fk_study_member_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);