-- Preserve products by assigning them to the owner of their category before
-- removing suppliers left without category assignments by the seed migration.
UPDATE product p
SET supplier_id = sc.supplier_id
FROM supplier_categories sc
WHERE sc.category = p.category
  AND NOT EXISTS (
      SELECT 1
      FROM supplier_categories current_category
      WHERE current_category.supplier_id = p.supplier_id
  );

DELETE FROM supplier s
WHERE NOT EXISTS (
    SELECT 1
    FROM supplier_categories sc
    WHERE sc.supplier_id = s.id
);
