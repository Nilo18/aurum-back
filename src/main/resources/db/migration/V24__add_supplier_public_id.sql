ALTER TABLE supplier
    ADD COLUMN public_id VARCHAR(36) DEFAULT gen_random_uuid()::text;

-- Backfill existing suppliers while preserving their internal IDs and relationships.
UPDATE supplier
SET public_id = gen_random_uuid()::text
WHERE public_id IS NULL;

ALTER TABLE supplier
    ALTER COLUMN public_id SET NOT NULL,
    ADD CONSTRAINT uq_supplier_public_id UNIQUE (public_id);
