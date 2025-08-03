--liquibase formatted sql

--changeset enes:1
--comment: Create flight table with sequence and departure_datetime field

-- Sequence oluştur.1'den başlar 50'şer artar.Flight entitydeki allocationSize ile aynı olmalıdır
CREATE SEQUENCE flight_seq START WITH 1 INCREMENT BY 50;

-- Flight tablosunu oluştur
CREATE TABLE flight (
    id BIGINT NOT NULL DEFAULT nextval('flight_seq'),
    flight_number VARCHAR(255) NOT NULL,
    departure_datetime TIMESTAMP,
    CONSTRAINT pk_flight PRIMARY KEY (id)
);

--rollback DROP TABLE flight; DROP SEQUENCE flight_seq;