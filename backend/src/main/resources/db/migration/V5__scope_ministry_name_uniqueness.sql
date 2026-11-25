ALTER TABLE ministries
    DROP INDEX uk_ministry_name,
    ADD CONSTRAINT uk_ministry_church_name UNIQUE (church_id, name);
