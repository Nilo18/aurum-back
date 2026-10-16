CREATE TABLE supplier_categories (
    supplier_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,

    PRIMARY KEY (supplier_id, category),

    FOREIGN KEY (supplier_id)
        REFERENCES supplier(id),

    UNIQUE (category)
);