-- Seed one owner per category, with SUP-006 supplying multiple categories.
-- Resolve supplier IDs by their seeded names instead of assuming identity values.
INSERT INTO supplier_categories (supplier_id, category)
SELECT s.id, assignment.category
FROM (VALUES
    ('SUP-006', 'FOOD_INGREDIENT'),
    ('SUP-006', 'TABLEWARE'),
    ('SUP-002', 'GLASSWARE'),
    ('SUP-004', 'FLORALS'),
    ('SUP-008', 'DECORATION')
) AS assignment(supplier_name, category)
JOIN supplier s ON s.name = assignment.supplier_name
ON CONFLICT (category) DO UPDATE
SET supplier_id = EXCLUDED.supplier_id;
