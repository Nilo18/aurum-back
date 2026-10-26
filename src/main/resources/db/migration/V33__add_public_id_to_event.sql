ALTER TABLE event
    ADD COLUMN public_id VARCHAR(36) DEFAULT gen_random_uuid()::text;

UPDATE event
SET public_id = gen_random_uuid()::text
WHERE public_id IS NULL;

ALTER TABLE event
    ALTER COLUMN public_id SET NOT NULL,
    ADD CONSTRAINT uq_event_public_id UNIQUE (public_id);