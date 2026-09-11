ALTER TABLE Invitations
    ADD COLUMN expires_at TIMESTAMPTZ NOT NULL CHECK (expires_at > NOW());