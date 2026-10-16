-- A category belongs to only one supplier. If legacy suppliers share a
-- product type, assign it to the supplier with the lowest ID.
-- Preserve any category assignments that have already been made.
INSERT INTO supplier_categories (supplier_id, category)
SELECT MIN(id), product_type
FROM supplier
GROUP BY product_type
ON CONFLICT (category) DO NOTHING;
