ALTER TABLE supplier_categories
    DROP CONSTRAINT supplier_categories_supplier_id_fkey,
    ADD CONSTRAINT supplier_categories_supplier_id_fkey
        FOREIGN KEY (supplier_id)
        REFERENCES supplier(id)
        ON DELETE CASCADE;

ALTER TABLE product
    ALTER COLUMN supplier_id DROP NOT NULL;

ALTER TABLE product
    DROP CONSTRAINT product_supplier_id_fkey,
    ADD CONSTRAINT product_supplier_id_fkey
        FOREIGN KEY (supplier_id)
        REFERENCES supplier(id)
        ON DELETE SET NULL;