ALTER TABLE feedback
    ADD COLUMN public_id VARCHAR(36) DEFAULT gen_random_uuid()::text;

UPDATE feedback
SET public_id = gen_random_uuid()::text
WHERE public_id IS NULL;

ALTER TABLE feedback
    ALTER COLUMN public_id SET NOT NULL,
    ADD CONSTRAINT uq_feedback_public_id UNIQUE (public_id);