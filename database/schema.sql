CREATE DATABASE IF NOT EXISTS gestorobras
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE gestorobras;

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255),
  email VARCHAR(255),
  password VARCHAR(255),
  created_at DATETIME(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS projects (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255),
  description VARCHAR(255),
  created_by BIGINT,
  created_at DATETIME(6),
  PRIMARY KEY (id),
  KEY idx_projects_created_by (created_by),
  CONSTRAINT fk_projects_created_by
    FOREIGN KEY (created_by)
    REFERENCES users (id)
    ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS project_users (
  project_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  PRIMARY KEY (project_id, user_id),
  KEY idx_project_users_user_id (user_id),
  CONSTRAINT fk_project_users_project
    FOREIGN KEY (project_id)
    REFERENCES projects (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_project_users_user
    FOREIGN KEY (user_id)
    REFERENCES users (id)
    ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_inputs (
  id BIGINT NOT NULL AUTO_INCREMENT,
  raw_text TEXT NOT NULL,
  project_id BIGINT NOT NULL,
  source VARCHAR(20),
  created_at DATETIME(6),
  PRIMARY KEY (id),
  KEY idx_user_inputs_project_id (project_id),
  CONSTRAINT fk_user_inputs_project
    FOREIGN KEY (project_id)
    REFERENCES projects (id)
    ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS records (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  description VARCHAR(255),
  type ENUM('INCIDENCIA', 'PENDIENTE', 'AVANCE') NOT NULL,
  status ENUM('ABIERTA', 'CERRADA'),
  project_id BIGINT,
  created_by BIGINT,
  related_record_id BIGINT,
  created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
  source_input_id BIGINT,
  structured_data JSON,
  PRIMARY KEY (id),
  KEY idx_records_project_id (project_id),
  KEY idx_records_title (title),
  KEY idx_records_created_by (created_by),
  KEY idx_records_source_input_id (source_input_id),
  KEY idx_records_related_record_id (related_record_id),
  CONSTRAINT fk_records_project
    FOREIGN KEY (project_id)
    REFERENCES projects (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_records_created_by
    FOREIGN KEY (created_by)
    REFERENCES users (id)
    ON DELETE SET NULL,
  CONSTRAINT fk_records_source_input
    FOREIGN KEY (source_input_id)
    REFERENCES user_inputs (id)
    ON DELETE SET NULL,
  CONSTRAINT fk_records_related_record
    FOREIGN KEY (related_record_id)
    REFERENCES records (id)
    ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS record_images (
  id BIGINT NOT NULL AUTO_INCREMENT,
  record_id BIGINT NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_record_images_record_id (record_id),
  CONSTRAINT fk_record_images_record
    FOREIGN KEY (record_id)
    REFERENCES records (id)
    ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
