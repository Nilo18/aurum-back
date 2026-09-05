ALTER TABLE client
    ADD COLUMN name VARCHAR(255),
    ADD COLUMN email VARCHAR(254),
    ADD COLUMN phone VARCHAR(30);

ALTER TABLE client
    ADD CONSTRAINT uq_client_email UNIQUE (email);