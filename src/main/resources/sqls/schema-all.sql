DROP TABLE IF EXISTS person;

CREATE TABLE person
(
    person_id  BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT, /*设置自增*/
    first_name VARCHAR(20),
    last_name  VARCHAR(20)
);