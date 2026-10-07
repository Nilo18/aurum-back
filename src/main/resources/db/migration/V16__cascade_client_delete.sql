-- Deleting a client also deletes their events. Event-owned records already
-- cascade through their event foreign keys; shared catalog records are retained.
ALTER TABLE event
    DROP CONSTRAINT event_client_id_fkey;

ALTER TABLE event
    ADD CONSTRAINT event_client_id_fkey
        FOREIGN KEY (client_id)
        REFERENCES client(id)
        ON DELETE CASCADE;
