CREATE TABLE ministries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(2000) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_ministry_name UNIQUE (name)
) ENGINE=InnoDB;

CREATE TABLE ministry_roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ministry_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_ministry_role_id_ministry UNIQUE (id, ministry_id),
    CONSTRAINT fk_ministry_role_ministry
        FOREIGN KEY (ministry_id) REFERENCES ministries (id)
) ENGINE=InnoDB;

CREATE TABLE volunteer_ministries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    volunteer_id BIGINT NOT NULL,
    ministry_id BIGINT NOT NULL,
    ministry_role_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_volunteer_ministry_role
        UNIQUE (volunteer_id, ministry_id, ministry_role_id),
    CONSTRAINT fk_volunteer_ministry_volunteer
        FOREIGN KEY (volunteer_id) REFERENCES volunteers (id),
    CONSTRAINT fk_volunteer_ministry_ministry
        FOREIGN KEY (ministry_id) REFERENCES ministries (id),
    CONSTRAINT fk_volunteer_ministry_role
        FOREIGN KEY (ministry_role_id, ministry_id)
        REFERENCES ministry_roles (id, ministry_id)
) ENGINE=InnoDB;
