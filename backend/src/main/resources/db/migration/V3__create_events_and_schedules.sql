CREATE TABLE service_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(150) NOT NULL,
    event_date DATE NOT NULL,
    event_time TIME NOT NULL,
    day_or_shift VARCHAR(40) NOT NULL,
    observacoes VARCHAR(2000) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AGENDADO',
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE schedule_assignments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    service_event_id BIGINT NOT NULL,
    ministry_id BIGINT NOT NULL,
    ministry_role_id BIGINT NOT NULL,
    volunteer_id BIGINT NOT NULL,
    confirmation_status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    PRIMARY KEY (id),
    CONSTRAINT uk_schedule_event_volunteer UNIQUE (service_event_id, volunteer_id),
    CONSTRAINT fk_schedule_event FOREIGN KEY (service_event_id) REFERENCES service_events (id),
    CONSTRAINT fk_schedule_ministry FOREIGN KEY (ministry_id) REFERENCES ministries (id),
    CONSTRAINT fk_schedule_role FOREIGN KEY (ministry_role_id) REFERENCES ministry_roles (id),
    CONSTRAINT fk_schedule_volunteer FOREIGN KEY (volunteer_id) REFERENCES volunteers (id)
) ENGINE=InnoDB;
