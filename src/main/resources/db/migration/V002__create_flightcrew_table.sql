--liquibase formatted sql

--changeset enes:2 
--comment: Create flight_crew table with foreign key to flight

-- Sequence oluştur
CREATE SEQUENCE flight_crew_seq START WITH 1 INCREMENT BY 50;

-- FlightCrew tablosunu oluştur
CREATE TABLE flight_crew (
    id BIGINT NOT NULL DEFAULT nextval('flight_crew_seq'),
    crew_name VARCHAR(255) NOT NULL,
    crew_type VARCHAR(50),
    flight_id BIGINT NOT NULL,
    CONSTRAINT pk_flight_crew PRIMARY KEY (id),
    CONSTRAINT fk_flight_crew_flight FOREIGN KEY (flight_id) REFERENCES flight(id)
);

--rollback DROP TABLE flight_crew; DROP SEQUENCE flight_crew_seq;