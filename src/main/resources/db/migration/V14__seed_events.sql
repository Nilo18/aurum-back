INSERT INTO event (
    client_id, event_type, date, total_cost,
    guest_count, location, notes, status
)
SELECT
    (SELECT id FROM client ORDER BY id LIMIT 1),
    event_type, event_date, total_cost,
    guest_count, location, notes, status
FROM (VALUES
    ('WEDDING', DATE '2026-10-03', 18000.00, 150,
     'AURUM_BANQUET_HALL', 'Classic wedding with floral decorations.', 'CONFIRMED'),

    ('CORPORATE EVENT', DATE '2026-10-05', 6500.00, 80,
     'CORPORATE_OFFICE', 'Annual team celebration with buffet service.', 'CONFIRMED'),

    ('CONFERENCE', DATE '2026-10-07', 12000.00, 200,
     'AURUM_CONFERENCE_HALL', 'Technology conference with coffee breaks.', 'PLANNING'),

    ('OFFICIAL RECEPTION', DATE '2026-10-09', 15000.00, 100,
     'HOTEL', 'Formal reception with plated dinner.', 'CONFIRMED'),

    ('ANNIVERSARY', DATE '2026-10-10', 4200.00, 45,
     'RESTAURANT', 'Silver wedding anniversary celebration.', 'PLANNING'),

    ('BIRTHDAY', DATE '2026-10-11', 2500.00, 30,
     'PRIVATE_RESIDENCE', 'Birthday dinner with custom cake.', 'REQUESTED'),

    ('GALA DINNER', DATE '2026-10-14', 22000.00, 180,
     'AURUM_BANQUET_HALL', 'Charity gala with live music.', 'PLANNING'),

    ('PRODUCT LAUNCH', DATE '2026-10-16', 9500.00, 120,
     'PARTNER_VENUE', 'Product presentation with cocktail reception.', 'CONFIRMED'),

    ('PRIVATE PARTY', DATE '2026-10-17', 3800.00, 50,
     'OUTDOOR_VENUE', 'Autumn garden party with seasonal menu.', 'REQUESTED'),

    ('OTHER', DATE '2026-10-19', 5000.00, 70,
     'HISTORICAL_VENUE', 'Cultural evening with Georgian cuisine.', 'PLANNING'),

    ('WEDDING', DATE '2026-10-24', 24000.00, 220,
     'HOTEL', 'Large wedding with welcome drinks and dinner.', 'CONFIRMED'),

    ('CORPORATE EVENT', DATE '2026-10-26', 7200.00, 90,
     'RESTAURANT', 'Employee recognition dinner.', 'PLANNING'),

    ('CONFERENCE', DATE '2026-10-28', 8500.00, 140,
     'AURUM_CONFERENCE_HALL', 'Business forum with lunch and refreshments.', 'REQUESTED'),

    ('OFFICIAL RECEPTION', DATE '2026-10-30', 11000.00, 85,
     'HISTORICAL_VENUE', 'Delegation welcome reception.', 'PLANNING'),

    ('ANNIVERSARY', DATE '2026-11-01', 6000.00, 65,
     'AURUM_BANQUET_HALL', 'Company tenth anniversary dinner.', 'CONFIRMED'),

    ('BIRTHDAY', DATE '2026-11-04', 1800.00, 25,
     'RESTAURANT', 'Small birthday celebration with dessert table.', 'REQUESTED'),

    ('GALA DINNER', DATE '2026-11-07', 28000.00, 250,
     'HOTEL', 'Awards ceremony with a three-course dinner.', 'PLANNING'),

    ('PRODUCT LAUNCH', DATE '2026-11-10', 7800.00, 95,
     'CORPORATE_OFFICE', 'New collection launch with finger food.', 'REQUESTED'),

    ('PRIVATE PARTY', DATE '2026-11-13', 3200.00, 40,
     'PRIVATE_RESIDENCE', 'Private dinner with vegetarian options.', 'CONFIRMED'),

    ('OTHER', DATE '2026-11-15', 4500.00, 60,
     'PARTNER_VENUE', 'Community fundraising dinner.', 'REQUESTED'),

    ('WEDDING', DATE '2026-09-05', 16000.00, 130,
     'OUTDOOR_VENUE', 'Outdoor wedding with evening buffet.', 'COMPLETED'),

    ('CORPORATE EVENT', DATE '2026-09-08', 5800.00, 75,
     'CORPORATE_OFFICE', 'Quarterly team gathering.', 'COMPLETED'),

    ('CONFERENCE', DATE '2026-09-12', 10000.00, 170,
     'AURUM_CONFERENCE_HALL', 'Education conference with two coffee breaks.', 'COMPLETED'),

    ('OFFICIAL RECEPTION', DATE '2026-09-15', 13500.00, 110,
     'HISTORICAL_VENUE', 'International guest reception.', 'COMPLETED'),

    ('ANNIVERSARY', DATE '2026-09-18', 3500.00, 35,
     'RESTAURANT', 'Family anniversary dinner.', 'COMPLETED'),

    ('BIRTHDAY', DATE '2026-10-21', 2200.00, 28,
     'PRIVATE_RESIDENCE', 'Cancelled following a change in client plans.', 'CANCELLED'),

    ('GALA DINNER', DATE '2026-11-20', 19000.00, 160,
     'AURUM_BANQUET_HALL', 'Request declined due to venue unavailability.', 'REJECTED'),

    ('PRODUCT LAUNCH', DATE '2026-11-22', 6800.00, 85,
     'PARTNER_VENUE', 'Launch cancelled after product release delay.', 'CANCELLED'),

    ('PRIVATE PARTY', DATE '2026-11-25', 4100.00, 55,
     'OTHER', 'Private celebration at a rented studio.', 'PLANNING'),

    ('OTHER', DATE '2026-11-28', 5500.00, 80,
     'AURUM_CONFERENCE_HALL', 'Networking evening with buffet and drinks.', 'REQUESTED')
) AS sample_events (
    event_type, event_date, total_cost,
    guest_count, location, notes, status
);