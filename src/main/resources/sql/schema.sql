DROP TABLE IF EXISTS users;
CREATE TABLE IF NOT EXISTS users  (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(200) NOT NULL UNIQUE,
    password_hash VARCHAR(200) NOT NULL,
    nickname VARCHAR(100) NOT NULL UNIQUE,
    region VARCHAR(100),
    image_key VARCHAR(500),
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

DROP TABLE IF EXISTS material_file;
DROP TABLE IF EXISTS material;
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
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    study_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    CONSTRAINT uk_study_condition UNIQUE (study_id, content),
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
  rank_point INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_study_member_study_user UNIQUE (study_id, user_id),
  CONSTRAINT fk_study_member_study FOREIGN KEY (study_id) REFERENCES study (id) ON DELETE CASCADE,
  CONSTRAINT fk_study_member_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE material (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    material_category VARCHAR(50) NOT NULL,
    week INT,
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    study_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_material_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_material_study FOREIGN KEY (study_id) REFERENCES study (id) ON DELETE CASCADE
);

CREATE INDEX idx_material_main_search
    ON material(study_id, material_category, created_at DESC);

CREATE TABLE material_file
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    file_key    VARCHAR(255) NOT NULL UNIQUE,
    size        BIGINT       NOT NULL,
    file_type   VARCHAR(50)  NOT NULL,
    processing_status VARCHAR(50),
    extracted_text    TEXT,
    material_id BIGINT       NOT NULL,
    CONSTRAINT fk_material_file_material FOREIGN KEY (material_id) REFERENCES material (id) ON DELETE CASCADE
);

CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status BOOLEAN NOT NULL,
    check_in_time TIMESTAMP NULL,
    CONSTRAINT fk_attendance_schedule FOREIGN KEY (schedule_id)
        REFERENCES schedule (id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS schedule (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        studyid BIGINT NOT NULL,
                                        title VARCHAR(255) NOT NULL,
    description TEXT NULL,
    starttime TIMESTAMP NOT NULL,
    endtime TIMESTAMP NOT NULL,
    createdat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_schedule__study FOREIGN KEY (studyid) REFERENCES study(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_schedule__study_start ON schedule (studyid, starttime);

CREATE TABLE IF NOT EXISTS schedule_tune (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             study_id BIGINT NOT NULL,
                                             title VARCHAR(255) NOT NULL,
    description TEXT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    available_start_time TIME NOT NULL,
    available_end_time TIME NOT NULL,
    slot_minutes INT NOT NULL DEFAULT 30,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_schedule_tune__study FOREIGN KEY (study_id) REFERENCES study(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_schedule_tune__study_status ON schedule_tune (study_id, status);

CREATE TABLE IF NOT EXISTS schedule_tune_participant (
                                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                         schedule_tune_id BIGINT NOT NULL,
                                                         study_member_id BIGINT NOT NULL,
                                                         candidate_number BIGINT NOT NULL,
                                                         voted_at TIMESTAMP NULL,
                                                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                         CONSTRAINT uk_schedule_tune_participant__tune_member UNIQUE (schedule_tune_id, study_member_id),
    CONSTRAINT fk_schedule_tune_participant__tune FOREIGN KEY (schedule_tune_id) REFERENCES schedule_tune(id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_tune_participant__studymember FOREIGN KEY (study_member_id) REFERENCES studymember(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_schedule_tune_participant__tune ON schedule_tune_participant (schedule_tune_id);

CREATE TABLE IF NOT EXISTS schedule_tune_slot (
                                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                  schedule_tune_id BIGINT NOT NULL,
                                                  slot_index INT NOT NULL,
                                                  start_time TIMESTAMP NOT NULL,
                                                  end_time TIMESTAMP NOT NULL,
                                                  occupancy_bits VARBINARY(512) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_schedule_tune_slot__tune_index UNIQUE (schedule_tune_id, slot_index),
    CONSTRAINT fk_schedule_tune_slot__tune FOREIGN KEY (schedule_tune_id) REFERENCES schedule_tune(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_schedule_tune_slot__tune_index ON schedule_tune_slot (schedule_tune_id, slot_index);

CREATE TABLE chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    study_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_message_study FOREIGN KEY (study_id) REFERENCES study (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_message_sender FOREIGN KEY (sender_id) REFERENCES study_member (id) ON DELETE CASCADE
);

CREATE INDEX idx_chat_message_study_id ON chat_message (study_id, id);
CREATE INDEX idx_chat_message_study_created_at ON chat_message (study_id, created_at);

CREATE TABLE IF NOT EXISTS chapter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    study_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_chapter_study FOREIGN KEY (study_id) REFERENCES study(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reflection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    study_id BIGINT NOT NULL,
    study_member_id BIGINT NOT NULL,
    schedule_id BIGINT NULL,
    satisfaction_score INT NOT NULL,
    understanding_score INT NOT NULL,
    participation_score INT NOT NULL,
    learned_content VARCHAR(1000) NOT NULL,
    improvement VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reflection_study FOREIGN KEY (study_id) REFERENCES study(id) ON DELETE CASCADE,
    CONSTRAINT fk_reflection_study_member FOREIGN KEY (study_member_id) REFERENCES study_member(id) ON DELETE CASCADE,
    CONSTRAINT fk_reflection_schedule FOREIGN KEY (schedule_id) REFERENCES schedule(id) ON DELETE SET NULL
);
