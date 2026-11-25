CREATE TABLE churches (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    cnpj VARCHAR(18) NULL,
    city VARCHAR(100) NULL,
    state VARCHAR(2) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE branches (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NULL,
    city VARCHAR(100) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    church_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_branch_church FOREIGN KEY (church_id) REFERENCES churches (id)
) ENGINE=InnoDB;

CREATE TABLE app_users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL,
    church_id BIGINT NULL,
    branch_id BIGINT NULL,
    volunteer_id BIGINT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_user_email (email),
    UNIQUE KEY uk_app_user_volunteer (volunteer_id),
    CONSTRAINT fk_app_user_church FOREIGN KEY (church_id) REFERENCES churches (id),
    CONSTRAINT fk_app_user_branch FOREIGN KEY (branch_id) REFERENCES branches (id),
    CONSTRAINT fk_app_user_volunteer FOREIGN KEY (volunteer_id) REFERENCES volunteers (id)
) ENGINE=InnoDB;

ALTER TABLE volunteers ADD COLUMN church_id BIGINT NULL, ADD COLUMN branch_id BIGINT NULL;
ALTER TABLE ministries ADD COLUMN church_id BIGINT NULL, ADD COLUMN branch_id BIGINT NULL;
ALTER TABLE service_events ADD COLUMN church_id BIGINT NULL, ADD COLUMN branch_id BIGINT NULL;
ALTER TABLE schedule_assignments ADD COLUMN church_id BIGINT NULL, ADD COLUMN branch_id BIGINT NULL;

INSERT INTO churches (name, city, state, active) VALUES ('Igreja Poiema', NULL, NULL, TRUE);

INSERT INTO branches (name, church_id, active)
SELECT 'Sede', id, TRUE FROM churches WHERE name = 'Igreja Poiema';

UPDATE volunteers SET church_id = (SELECT id FROM churches WHERE name = 'Igreja Poiema' ORDER BY id LIMIT 1)
WHERE church_id IS NULL;
UPDATE volunteers SET branch_id = (SELECT b.id FROM branches b JOIN churches c ON c.id = b.church_id
WHERE c.name = 'Igreja Poiema' AND b.name = 'Sede' ORDER BY b.id LIMIT 1) WHERE branch_id IS NULL;
UPDATE ministries SET church_id = (SELECT id FROM churches WHERE name = 'Igreja Poiema' ORDER BY id LIMIT 1)
WHERE church_id IS NULL;
UPDATE ministries SET branch_id = (SELECT b.id FROM branches b JOIN churches c ON c.id = b.church_id
WHERE c.name = 'Igreja Poiema' AND b.name = 'Sede' ORDER BY b.id LIMIT 1) WHERE branch_id IS NULL;
UPDATE service_events SET church_id = (SELECT id FROM churches WHERE name = 'Igreja Poiema' ORDER BY id LIMIT 1)
WHERE church_id IS NULL;
UPDATE service_events SET branch_id = (SELECT b.id FROM branches b JOIN churches c ON c.id = b.church_id
WHERE c.name = 'Igreja Poiema' AND b.name = 'Sede' ORDER BY b.id LIMIT 1) WHERE branch_id IS NULL;
UPDATE schedule_assignments sa JOIN service_events e ON e.id = sa.service_event_id
SET sa.church_id = e.church_id, sa.branch_id = e.branch_id WHERE sa.church_id IS NULL;

ALTER TABLE volunteers MODIFY church_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_volunteer_church FOREIGN KEY (church_id) REFERENCES churches (id),
    ADD CONSTRAINT fk_volunteer_branch FOREIGN KEY (branch_id) REFERENCES branches (id);
ALTER TABLE ministries MODIFY church_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_ministry_church FOREIGN KEY (church_id) REFERENCES churches (id),
    ADD CONSTRAINT fk_ministry_branch FOREIGN KEY (branch_id) REFERENCES branches (id);
ALTER TABLE service_events MODIFY church_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_event_church FOREIGN KEY (church_id) REFERENCES churches (id),
    ADD CONSTRAINT fk_event_branch FOREIGN KEY (branch_id) REFERENCES branches (id);
ALTER TABLE schedule_assignments MODIFY church_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_schedule_church FOREIGN KEY (church_id) REFERENCES churches (id),
    ADD CONSTRAINT fk_schedule_branch FOREIGN KEY (branch_id) REFERENCES branches (id);
