-- 전 준비: 데이터 전부 삭제됨. 반드시 백업 후 실행.
SET FOREIGN_KEY_CHECKS = 0;

-- 1) 기존 테이블 삭제
DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS material_file;
DROP TABLE IF EXISTS material;
DROP TABLE IF EXISTS schedule_tune_participant;
DROP TABLE IF EXISTS schedule_tune_slot;
DROP TABLE IF EXISTS schedule_tune;
DROP TABLE IF EXISTS schedule;
DROP TABLE IF EXISTS study_application;
DROP TABLE IF EXISTS study_category;
DROP TABLE IF EXISTS study_condition;
DROP TABLE IF EXISTS chat_message;
DROP TABLE IF EXISTS reflection;
DROP TABLE IF EXISTS chapter;
DROP TABLE IF EXISTS quiz_point_log;
DROP TABLE IF EXISTS answer_submission;
DROP TABLE IF EXISTS quiz_submission;
DROP TABLE IF EXISTS quiz_choice;
DROP TABLE IF EXISTS multiple_choice_question;
DROP TABLE IF EXISTS short_answer_question;
DROP TABLE IF EXISTS quiz_question;
DROP TABLE IF EXISTS quiz_file_source;
DROP TABLE IF EXISTS quiz;
DROP TABLE IF EXISTS study_member;
DROP TABLE IF EXISTS study;
DROP TABLE IF EXISTS user_interests;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS email_verifications;


SET FOREIGN_KEY_CHECKS = 1;

-- 2) 테이블 생성 (엔진/문자셋 지정)
CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       email VARCHAR(200) NOT NULL,
                       password_hash VARCHAR(200) NOT NULL,
                       nickname VARCHAR(100) NOT NULL,
                       region ENUM ('ONLINE','SEOUL','GYEONGGI','INCHEON','GANGWON','DAEJEON','SEJONG','CHUNGNAM','CHUNGBUK','GWANGJU','JEONNAM','JEONBUK','DAEGU','GYEONGBUK','BUSAN','ULSAN','GYEONGNAM','JEJU'),
                       image_key VARCHAR(500),
                       gender ENUM ('MALE','FEMALE') NOT NULL,
                       created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                       updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                       PRIMARY KEY (id),
                       UNIQUE KEY uk_users_email (email),
                       UNIQUE KEY uk_users_nickname (nickname)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_interests (
                                id BIGINT NOT NULL AUTO_INCREMENT,
                                user_id BIGINT NOT NULL,
                                category ENUM ('LANGUAGE','EMPLOYMENT','EXAM','HOBBY','PROGRAMMING','AUTONOMY') NOT NULL,
                                PRIMARY KEY (id),
                                UNIQUE KEY uk_user_category (user_id, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE study (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       leader_id BIGINT NOT NULL,
                       title VARCHAR(100) NOT NULL,
                       description VARCHAR(200) NOT NULL,
                       detail_description TEXT NULL,
                       study_time VARCHAR(100) NULL,
                       max_members INT NOT NULL,
                       file_key VARCHAR(500) NULL,
                       region ENUM ('ONLINE','SEOUL','GYEONGGI','INCHEON','GANGWON','DAEJEON','SEJONG','CHUNGNAM','CHUNGBUK','GWANGJU','JEONNAM','JEONBUK','DAEGU','GYEONGBUK','BUSAN','ULSAN','GYEONGNAM','JEJU') NOT NULL,
                       status ENUM ('RECRUITING','IN_PROGRESS','FINISHED') NOT NULL,
                       created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                       updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                       PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE study_member (
                              id BIGINT NOT NULL AUTO_INCREMENT,
                              study_id BIGINT NOT NULL,
                              user_id BIGINT NOT NULL,
                              role ENUM ('LEADER','MEMBER') NOT NULL,
                              message VARCHAR(500) NULL,
                              rank_point INT NOT NULL DEFAULT 0,
                              created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                              updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                              PRIMARY KEY (id),
                              UNIQUE KEY uk_study_member_study_user (study_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE schedule (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          start_time TIMESTAMP(6) NOT NULL,
                          end_time TIMESTAMP(6) NOT NULL,
                          created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                          updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                          study_id BIGINT NOT NULL,
                          description TEXT NULL,
                          title VARCHAR(255) NOT NULL,
                          PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE attendance (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            status BOOLEAN NOT NULL,
                            check_in_time TIMESTAMP(6) NULL,
                            schedule_id BIGINT NOT NULL,
                            user_id BIGINT NOT NULL,
                            PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE material (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          week INT NULL,
                          created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                          updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                          study_id BIGINT NOT NULL,
                          user_id BIGINT NOT NULL,
                          content TEXT NOT NULL,
                          title VARCHAR(255) NOT NULL,
                          material_category ENUM ('NOTICE','LEARNING','ASSIGNMENT') NOT NULL,
                          PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE material_file (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               material_id BIGINT NOT NULL,
                               size BIGINT NOT NULL,
                               processing_status VARCHAR(50),
                               extracted_text TEXT,
                               file_key VARCHAR(255) NOT NULL,
                               file_type VARCHAR(255) NOT NULL,
                               name VARCHAR(255) NOT NULL,
                               PRIMARY KEY (id),
                               UNIQUE KEY uk_material_file_key (file_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE schedule_tune (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               available_start_time TIME(6) NOT NULL,
                               available_end_time TIME(6) NOT NULL,
                               start_date DATE NOT NULL,
                               end_date DATE NOT NULL,
                               slot_minutes INT NOT NULL,
                               created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                               updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                               study_id BIGINT NOT NULL,
                               description TEXT NULL,
                               title VARCHAR(255) NOT NULL,
                               status ENUM ('PENDING','COMPLETED') NOT NULL,
                               PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE schedule_tune_participant (
                                           id BIGINT NOT NULL AUTO_INCREMENT,
                                           schedule_tune_id BIGINT NOT NULL,
                                           study_member_id BIGINT NOT NULL,
                                           candidate_number BIGINT NOT NULL,
                                           created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                           updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                                           voted_at TIMESTAMP(6) NULL,
                                           PRIMARY KEY (id),
                                           UNIQUE KEY uk_schedule_tune_participant__tune_member (schedule_tune_id, study_member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE schedule_tune_slot (
                                    id BIGINT NOT NULL AUTO_INCREMENT,
                                    schedule_tune_id BIGINT NOT NULL,
                                    slot_index INT NOT NULL,
                                    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                                    start_time TIMESTAMP(6) NOT NULL,
                                    end_time TIMESTAMP(6) NOT NULL,
                                    occupancy_bits VARBINARY(512) NOT NULL,
                                    PRIMARY KEY (id),
                                    UNIQUE KEY uk_schedule_tune_slot__tune_index (schedule_tune_id, slot_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE study_application (
                                   id BIGINT NOT NULL AUTO_INCREMENT,
                                   study_id BIGINT NOT NULL,
                                   user_id BIGINT NOT NULL,
                                   status ENUM ('PENDING','APPROVED','REJECTED') NOT NULL,
                                   message VARCHAR(500) NULL,
                                   created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                   updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                                   PRIMARY KEY (id),
                                   UNIQUE KEY uk_study_application_study_user (study_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE study_category (
                                id BIGINT NOT NULL AUTO_INCREMENT,
                                study_id BIGINT NOT NULL,
                                category ENUM ('LANGUAGE','EMPLOYMENT','EXAM','HOBBY','PROGRAMMING','AUTONOMY') NOT NULL,
                                PRIMARY KEY (id),
                                UNIQUE KEY uk_study_category_study_category (study_id, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE study_condition (
                                 id BIGINT NOT NULL AUTO_INCREMENT,
                                 study_id BIGINT NOT NULL,
                                 content VARCHAR(500) NOT NULL,
                                 PRIMARY KEY (id),
                                 UNIQUE KEY uk_study_condition (study_id, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chat_message (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              study_id BIGINT NOT NULL,
                              sender_id BIGINT NOT NULL,
                              content TEXT NOT NULL,
                              created_at TIMESTAMP NOT NULL,
                              updated_at TIMESTAMP NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chapter (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         study_id BIGINT NOT NULL,
                         content VARCHAR(500) NOT NULL,
                         completed BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reflection (
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
                            created_at TIMESTAMP NOT NULL,
                            updated_at TIMESTAMP NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE quiz (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      study_id BIGINT NOT NULL,
                      created_by BIGINT NOT NULL,
                      title VARCHAR(255) NOT NULL,
                      time_limit_seconds INT,
                      status VARCHAR(50) NOT NULL,
                      created_at TIMESTAMP NOT NULL,
                      updated_at TIMESTAMP NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE quiz_file_source (
                                  quiz_id BIGINT NOT NULL,
                                  file_id BIGINT NOT NULL,
                                  PRIMARY KEY (quiz_id, file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE quiz_question (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               quiz_id BIGINT NOT NULL,
                               question_text TEXT NOT NULL,
                               question_type VARCHAR(31) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE short_answer_question (
                                       id BIGINT PRIMARY KEY,
                                       answer VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE multiple_choice_question (
                                          id BIGINT PRIMARY KEY,
                                          correct_choice_id BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE quiz_choice (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             question_id BIGINT NOT NULL,
                             choice_text VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE quiz_submission (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 quiz_id BIGINT NOT NULL,
                                 user_id BIGINT NOT NULL,
                                 score INT NOT NULL,
                                 total_questions INT NOT NULL,
                                 status VARCHAR(255) NOT NULL,
                                 submitted_at TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE answer_submission (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   submission_id BIGINT NOT NULL,
                                   question_id BIGINT NOT NULL,
                                   submitted_answer TEXT,
                                   is_correct BOOLEAN NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE quiz_point_log (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                study_member_id BIGINT NOT NULL,
                                quiz_submission_id BIGINT NOT NULL UNIQUE,
                                points_awarded INT NOT NULL,
                                created_at TIMESTAMP NOT NULL,
                                revoked BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 3) 인덱스
CREATE INDEX idx_study_status_created_at ON study (status, created_at);
CREATE INDEX idx_study_region ON study (region);
CREATE INDEX idx_study_category_study_id ON study_category (study_id);
CREATE INDEX idx_study_category_category ON study_category (category);
CREATE INDEX idx_schedule_study_start ON schedule (study_id, start_time);
CREATE INDEX idx_chat_message_study_id ON chat_message (study_id, id);
CREATE INDEX idx_chat_message_study_created_at ON chat_message (study_id, created_at);


-- 4) 외래키
ALTER TABLE user_interests ADD CONSTRAINT fk_user_interests_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE study ADD CONSTRAINT fk_study_leader FOREIGN KEY (leader_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE study_member ADD CONSTRAINT fk_study_member_study FOREIGN KEY (study_id) REFERENCES study(id) ON DELETE CASCADE;
ALTER TABLE study_member ADD CONSTRAINT fk_study_member_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE study_application ADD CONSTRAINT fk_study_application_study FOREIGN KEY (study_id) REFERENCES study(id) ON DELETE CASCADE;
ALTER TABLE study_application ADD CONSTRAINT fk_study_application_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE study_category ADD CONSTRAINT fk_study_category_study FOREIGN KEY (study_id) REFERENCES study(id) ON DELETE CASCADE;
ALTER TABLE study_condition ADD CONSTRAINT fk_study_condition_study FOREIGN KEY (study_id) REFERENCES study(id) ON DELETE CASCADE;
ALTER TABLE schedule ADD CONSTRAINT fk_schedule_study FOREIGN KEY (study_id) REFERENCES study(id) ON DELETE CASCADE;
ALTER TABLE attendance ADD CONSTRAINT fk_attendance_schedule FOREIGN KEY (schedule_id) REFERENCES schedule(id) ON DELETE CASCADE;
ALTER TABLE attendance ADD CONSTRAINT fk_attendance_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE material ADD CONSTRAINT fk_material_study FOREIGN KEY (study_id) REFERENCES study(id) ON DELETE CASCADE;
ALTER TABLE material ADD CONSTRAINT fk_material_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE material_file ADD CONSTRAINT fk_material_file_material FOREIGN KEY (material_id) REFERENCES material(id) ON DELETE CASCADE;
ALTER TABLE schedule_tune ADD CONSTRAINT fk_schedule_tune_study FOREIGN KEY (study_id) REFERENCES study(id) ON DELETE CASCADE;
ALTER TABLE schedule_tune_participant ADD CONSTRAINT fk_participant_tune FOREIGN KEY (schedule_tune_id) REFERENCES schedule_tune(id) ON DELETE CASCADE;
ALTER TABLE schedule_tune_participant ADD CONSTRAINT fk_schedule_tune_participant_study_member FOREIGN KEY (study_member_id) REFERENCES study_member(id) ON DELETE CASCADE;
ALTER TABLE schedule_tune_slot ADD CONSTRAINT fk_slot_tune FOREIGN KEY (schedule_tune_id) REFERENCES schedule_tune(id) ON DELETE CASCADE;
ALTER TABLE chat_message ADD CONSTRAINT fk_chat_message_study FOREIGN KEY (study_id) REFERENCES study(id) ON DELETE CASCADE;
ALTER TABLE chat_message ADD CONSTRAINT fk_chat_message_sender FOREIGN KEY (sender_id) REFERENCES study_member(id) ON DELETE CASCADE;
ALTER TABLE chapter ADD CONSTRAINT fk_chapter_study FOREIGN KEY (study_id) REFERENCES study(id) ON DELETE CASCADE;
ALTER TABLE reflection ADD CONSTRAINT fk_reflection_study FOREIGN KEY (study_id) REFERENCES study(id) ON DELETE CASCADE;
ALTER TABLE reflection ADD CONSTRAINT fk_reflection_study_member FOREIGN KEY (study_member_id) REFERENCES study_member(id) ON DELETE CASCADE;
ALTER TABLE reflection ADD CONSTRAINT fk_reflection_schedule FOREIGN KEY (schedule_id) REFERENCES schedule(id) ON DELETE SET NULL;
ALTER TABLE quiz ADD CONSTRAINT fk_quiz_study FOREIGN KEY (study_id) REFERENCES study(id) ON DELETE CASCADE;
ALTER TABLE quiz ADD CONSTRAINT fk_quiz_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE quiz_file_source ADD CONSTRAINT fk_qfs_quiz FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE;
ALTER TABLE quiz_file_source ADD CONSTRAINT fk_qfs_file FOREIGN KEY (file_id) REFERENCES material_file(id) ON DELETE CASCADE;
ALTER TABLE quiz_question ADD CONSTRAINT fk_qq_quiz FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE;
ALTER TABLE short_answer_question ADD CONSTRAINT fk_saq_qq FOREIGN KEY (id) REFERENCES quiz_question(id) ON DELETE CASCADE;
ALTER TABLE multiple_choice_question ADD CONSTRAINT fk_mcq_qq FOREIGN KEY (id) REFERENCES quiz_question(id) ON DELETE CASCADE;
ALTER TABLE quiz_choice ADD CONSTRAINT fk_qc_mcq FOREIGN KEY (question_id) REFERENCES multiple_choice_question(id) ON DELETE CASCADE;
ALTER TABLE multiple_choice_question ADD CONSTRAINT fk_mcq_correct_choice FOREIGN KEY (correct_choice_id) REFERENCES quiz_choice(id) ON DELETE SET NULL;
ALTER TABLE quiz_submission ADD CONSTRAINT fk_qs_quiz FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE;
ALTER TABLE quiz_submission ADD CONSTRAINT fk_qs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE answer_submission ADD CONSTRAINT fk_as_submission FOREIGN KEY (submission_id) REFERENCES quiz_submission(id) ON DELETE CASCADE;
ALTER TABLE answer_submission ADD CONSTRAINT fk_as_question FOREIGN KEY (question_id) REFERENCES quiz_question(id) ON DELETE CASCADE;
ALTER TABLE quiz_point_log ADD CONSTRAINT fk_qpl_studymember FOREIGN KEY (study_member_id) REFERENCES study_member(id) ON DELETE CASCADE;
ALTER TABLE quiz_point_log ADD CONSTRAINT fk_qpl_quizsubmission FOREIGN KEY (quiz_submission_id) REFERENCES quiz_submission(id) ON DELETE CASCADE;