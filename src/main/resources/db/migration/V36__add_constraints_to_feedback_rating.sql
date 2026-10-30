ALTER TABLE feedback
ADD CONSTRAINT feedback_rating_range
CHECK (rating BETWEEN 1 AND 5);