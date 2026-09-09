ALTER TABLE Employees
    DROP COLUMN IF EXISTS first_name,
    DROP COLUMN IF EXISTS last_name,
    ADD COLUMN name VARCHAR(255) NOT NULL,
    ADD COLUMN email VARCHAR(255) NOT NULL CHECK (email ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$'),
    ADD COLUMN role VARCHAR(100) NOT NULL CHECK (role IN (
        'OWNER',
        'ADMIN',
        'STAFF'
    )),
    ADD COLUMN password TEXT NOT NULL;