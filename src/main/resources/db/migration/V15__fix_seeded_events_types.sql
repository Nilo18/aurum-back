BEGIN;

ALTER TABLE event
    DROP CONSTRAINT IF EXISTS event_event_type_check;

UPDATE event
SET event_type = REPLACE(event_type, ' ', '_')
WHERE event_type LIKE '% %';

ALTER TABLE event
    ADD CONSTRAINT event_event_type_check
    CHECK (event_type IN (
        'WEDDING',
        'CORPORATE_EVENT',
        'CONFERENCE',
        'OFFICIAL_RECEPTION',
        'ANNIVERSARY',
        'BIRTHDAY',
        'GALA_DINNER',
        'PRODUCT_LAUNCH',
        'PRIVATE_PARTY',
        'OTHER'
    ));

COMMIT;