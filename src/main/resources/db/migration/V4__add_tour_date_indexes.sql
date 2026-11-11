-- V4: Add indexes on tour date columns for date-range search optimization
CREATE INDEX IF NOT EXISTS idx_tours_start_date ON tours(start_date);
CREATE INDEX IF NOT EXISTS idx_tours_end_date ON tours(end_date);
CREATE INDEX IF NOT EXISTS idx_tours_date_range ON tours(start_date, end_date);

