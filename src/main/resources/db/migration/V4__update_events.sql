DROP TABLE IF EXISTS event_requests;

ALTER TABLE event
    DROP COLUMN IF EXISTS approximate_budget,
    DROP COLUMN IF EXISTS client_name;

ALTER TABLE event
    ADD COLUMN IF NOT EXISTS notes VARCHAR(500),
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED';

ALTER TABLE event
    ADD CONSTRAINT chk_event_status
    CHECK (status IN (
        'REQUESTED',
        'PLANNING',
        'CONFIRMED',
        'COMPLETED',
        'REJECTED',
        'CANCELLED'
    ));