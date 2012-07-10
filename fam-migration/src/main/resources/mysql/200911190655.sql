SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

CREATE TABLE booking (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  username VARCHAR(255) NOT NULL,
  seton TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status_id INTEGER UNSIGNED NOT NULL,
  status_seton TIMESTAMP NOT NULL,
  deviceKey VARCHAR(255) NOT NULL,
  capacityUnits INTEGER NOT NULL,
  time_end TIMESTAMP NULL,
  time_start TIMESTAMP NULL,
  cancelation_username VARCHAR(255) DEFAULT NULL,
  cancelation_reason VARCHAR(255) DEFAULT NULL,
  cancelation_seton TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY(id)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE TABLE deviceavailibility (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  available INTEGER UNSIGNED NOT NULL,
  timestampset TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  tfend TIMESTAMP NOT NULL,
  tfstart TIMESTAMP NOT NULL,
  tfinterval INTEGER UNSIGNED NOT NULL DEFAULT 0,
  deviceKey VARCHAR(255) NOT NULL,
  notice VARCHAR(255) NOT NULL,
  usernameSetThis VARCHAR(255) NOT NULL,
  PRIMARY KEY(id)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE TABLE usermail (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  username VARCHAR(255) NULL,
  subject VARCHAR(255) NULL,
  msg TEXT NULL,
  recipient VARCHAR(255) NULL,
  toSendDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  wasSentDate TIMESTAMP NULL,
  PRIMARY KEY(id)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE TABLE user (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  username VARCHAR(255) NOT NULL,
  registration TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  lastloging TIMESTAMP NULL DEFAULT NULL,
  birthdate TIMESTAMP NULL DEFAULT NULL,
  male TINYINT NULL DEFAULT NULL,
  excluded TINYINT DEFAULT NULL,
  statementOfAgreementAccepted TINYINT NOT NULL DEFAULT 0,
  locale_id VARCHAR(255) NOT NULL DEFAULT "en",
  pazzword VARCHAR(255) NOT NULL,
  phone1 VARCHAR(255) NULL,
  phone2 VARCHAR(255) NULL,
  company VARCHAR(255) NULL,
  fname VARCHAR(255) NULL,
  title VARCHAR(255) NULL,
  mail VARCHAR(255) NOT NULL,
  sname VARCHAR(255) NULL,
  roleid VARCHAR(255) NOT NULL,
  mainaddress_id INTEGER NULL,
  passwordEncoded TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY(id),
  INDEX address_FKIndex1(mainaddress_id)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE TABLE address (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id INTEGER UNSIGNED NOT NULL,
  zipcode VARCHAR(255) NULL,
  street VARCHAR(255) NULL,
  streetno VARCHAR(255) NULL,
  city VARCHAR(255) NULL,
  country VARCHAR(255) NULL,
  PRIMARY KEY(id),
  INDEX user_FKIndex1(user_id)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE TABLE logbookentry (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  logbookId VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  headline VARCHAR(255) NOT NULL,
  ofUserName VARCHAR(255) NULL,
  locale_id VARCHAR(255) NOT NULL DEFAULT "en",
  dateMade TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  tags VARCHAR(255) NOT NULL DEFAULT "en",
  PRIMARY KEY(id)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;
ALTER TABLE user change COLUMN birthdate birthdate DATE NULL DEFAULT NULL;
CREATE TABLE cronjobaction (
      id INTEGER UNSIGNED NOT NULL,
      nextResolveAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      description VARCHAR(255) NOT NULL,
      resolveEveryMinutes INTEGER NOT NULL,
      PRIMARY KEY(id)
    ) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

INSERT INTO cronjobaction(id, nextResolveAt, description, resolveEveryMinutes) VALUES(1, null, "mail: send saved and unsent", 5);
INSERT INTO cronjobaction(id, nextResolveAt, description, resolveEveryMinutes) VALUES(2, null, "mail: applications for system", 1440);
INSERT INTO cronjobaction(id, nextResolveAt, description, resolveEveryMinutes) VALUES(3, null, "mail: applications for facility", 1440);
ALTER TABLE deviceavailibility CHANGE COLUMN notice notice VARCHAR(255) DEFAULT NULL;
INSERT INTO cronjobaction(id, nextResolveAt, description, resolveEveryMinutes) VALUES(4, null, "mail: sent really want to book?", 5);
ALTER TABLE deviceavailibility CHANGE COLUMN tfend tfend DATETIME NOT NULL;
ALTER TABLE deviceavailibility CHANGE COLUMN tfstart tfstart DATETIME NOT NULL;
DELETE FROM cronjobaction WHERE id = 4;
ALTER TABLE booking ADD COLUMN notice TEXT DEFAULT NULL;
ALTER TABLE booking ADD COLUMN idBookedInBookingStrategy INT DEFAULT 1;
CREATE TABLE assession (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  booking_id INT NOT NULL,
  PRIMARY KEY(id),
  INDEX booking_FKIndex1(booking_id)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE TABLE assession_question (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  assession_id INT NOT NULL,
  question VARCHAR(255) NOT NULL,
  required TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY(id),
  INDEX assession_FKIndex1(assession_id)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE TABLE assession_question_givenanswer (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  assession_question_id INT NOT NULL,
  answer VARCHAR(255) NOT NULL,
  PRIMARY KEY(id),
  INDEX assession_question_FKIndex1(assession_question_id)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE TABLE assession_question_possibleanswer (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  assession_question_id INT NOT NULL,
  possibility VARCHAR(255) NOT NULL,
  selected TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY(id),
  INDEX assession_question_FKIndex1(assession_question_id)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;
DELETE FROM assession;
DELETE FROM assession_question;
DELETE FROM assession_question_givenanswer;
DELETE FROM assession_question_possibleanswer;
ALTER TABLE assession_question ADD COLUMN htmlInputId INT NOT NULL DEFAULT 1;
ALTER TABLE booking ADD COLUMN processed TINYINT NOT NULL DEFAULT 0;
UPDATE booking SET processed = 1;
ALTER TABLE assession_question ADD COLUMN jobstep_id INT NOT NULL DEFAULT 1;
ALTER TABLE deviceavailibility RENAME deviceavailability;
delete from deviceavailability where available = 5;
ALTER TABLE deviceavailability CHANGE COLUMN notice notice TEXT NULL DEFAULT NULL;