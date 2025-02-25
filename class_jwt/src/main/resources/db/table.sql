create table users(
    id              BIGINT          AUTO_INCREMENT  PRIMARY KEY,
    username        varchar(255)    NOT NULL,
    password        varchar(255)    NOT NULL,
    refresh_token   varchar(512)
);