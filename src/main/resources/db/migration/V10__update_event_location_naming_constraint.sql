ALTER TABLE Event
    DROP CONSTRAINT event_location_check,
    ADD CONSTRAINT event_location_check CHECK (location IN (
        'AURUM_BANQUET_HALL',
        'AURUM_CONFERENCE_HALL',
        'PRIVATE_RESIDENCE',
        'PARTNER_VENUE',
        'HOTEL',
        'RESTAURANT',
        'OUTDOOR_VENUE',
        'HISTORICAL_VENUE',
        'CORPORATE_OFFICE',
        'OTHER'
    ));