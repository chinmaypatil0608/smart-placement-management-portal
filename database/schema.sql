-- Optional reference DDL for a NEW database only.
-- Existing installations should be backed up and upgraded with Hibernate ddl-auto=update.
CREATE DATABASE IF NOT EXISTS placement_portal;
USE placement_portal;

CREATE TABLE IF NOT EXISTS students (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  department VARCHAR(255) NOT NULL,
  cgpa DOUBLE NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS jobs (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company VARCHAR(255) NOT NULL,
  title VARCHAR(255) NOT NULL,
  required_skills VARCHAR(1000) NOT NULL,
  minimum_cgpa DOUBLE NOT NULL,
  location VARCHAR(255) NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS job_applications (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  student_id BIGINT NOT NULL,
  job_id BIGINT NOT NULL,
  status VARCHAR(255) NULL,
  applied_at DATETIME(6) NULL,
  CONSTRAINT uq_application_student_job UNIQUE (student_id, job_id),
  CONSTRAINT fk_application_student FOREIGN KEY (student_id) REFERENCES students(id),
  CONSTRAINT fk_application_job FOREIGN KEY (job_id) REFERENCES jobs(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_accounts (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(255) NOT NULL,
  student_id BIGINT NULL UNIQUE,
  created_at DATETIME(6) NULL,
  CONSTRAINT fk_user_student FOREIGN KEY (student_id) REFERENCES students(id)
) ENGINE=InnoDB;
