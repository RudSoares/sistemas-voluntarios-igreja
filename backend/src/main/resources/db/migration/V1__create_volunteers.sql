CREATE TABLE volunteers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(40) NULL,
    email VARCHAR(254) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    observations TEXT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE volunteer_availability_days (
    volunteer_id BIGINT NOT NULL,
    availability_day VARCHAR(40) NOT NULL,
    PRIMARY KEY (volunteer_id, availability_day),
    CONSTRAINT fk_volunteer_availability_volunteer
        FOREIGN KEY (volunteer_id)
        REFERENCES volunteers (id)
        ON DELETE CASCADE
) ENGINE=InnoDB;
