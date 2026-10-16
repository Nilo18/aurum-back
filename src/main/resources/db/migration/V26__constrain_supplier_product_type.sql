-- Convert the legacy food descriptions inserted by V22 to the enum value.
UPDATE supplier
SET product_type = 'FOOD_INGREDIENT'
WHERE product_type IN (
    'Fresh vegetables', 'Fresh fruit', 'Dairy products', 'Meat and poultry',
    'Fish and seafood', 'Bakery products', 'Grains and legumes',
    'Spices and seasonings', 'Beverages', 'Confectionery'
);

ALTER TABLE supplier
    ADD CONSTRAINT supplier_product_type_check CHECK (product_type IN (
        'TABLEWARE', 'GLASSWARE', 'FLORALS', 'DECORATION', 'FOOD_INGREDIENT'
    ));
