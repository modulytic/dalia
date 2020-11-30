-- Noah Sandman <noah@modulytic.com>
-- 21 Oct 2020

CREATE DATABASE IF NOT EXISTS dalia;
GRANT ALL ON `dalia`.* TO 'dalia'@'localhost' IDENTIFIED BY 'password';

USE dalia;

-- auto-generated definition
-- Table to keep track of current DLR statuses
CREATE TABLE IF NOT EXISTS dlr_status
(
    msg_id       CHAR(36)                              NOT NULL,
    src_addr     VARCHAR(16)                           NOT NULL,
    dst_addr     VARCHAR(16)                           NOT NULL,
    msg_status   VARCHAR(16)                           NULL,
    submit_date  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    failure_only BOOLEAN     DEFAULT FALSE             NULL,
    intermediate BOOLEAN     DEFAULT FALSE             NULL,
    smpp_user    VARCHAR(36)                           NOT NULL,

    CONSTRAINT   dlr_status_msg_id_uindex unique      (msg_id),
    PRIMARY KEY (msg_id)
);

-- Table to store log of all sent messages for billing purposes
CREATE TABLE IF NOT EXISTS billing_logs
(
    msg_id       CHAR(36)                             NOT NULL,
    vroute       SMALLINT                             NULL,
    smpp_user    VARCHAR(36)                          NOT NULL, 
    rate         FLOAT                                NULL,
    country_code SMALLINT                             NOT NULL,
    sent_date    TIMESTAMP  DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT   billing_logs_msg_id_uindex unique   (msg_id),
    PRIMARY KEY (msg_id)
);

-- Table to store all our rates for our vroute
CREATE TABLE IF NOT EXISTS billing_vroutes
(
    id           SMALLINT                 NOT NULL AUTO_INCREMENT,
    vroute_name  VARCHAR(16)              NOT NULL,
    rate         FLOAT                    NOT NULL,
    country_code SMALLINT                 NOT NULL,
    is_active    BOOLEAN     DEFAULT TRUE NULL,

    UNIQUE (country_code, is_active),
    UNIQUE (vroute_name),
    PRIMARY KEY (id)
);
