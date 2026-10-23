ALTER TABLE menu_items
    ADD COLUMN public_id VARCHAR(36) DEFAULT gen_random_uuid()::text;

UPDATE menu_items
SET public_id = gen_random_uuid()::text
WHERE public_id IS NULL;

ALTER TABLE menu_items
    ALTER COLUMN public_id SET NOT NULL,
    ADD CONSTRAINT uq_menu_item_public_id UNIQUE (public_id);