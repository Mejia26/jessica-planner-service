CREATE TABLE IF NOT EXISTS calendar_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_user_id UUID NOT NULL,
    category_key VARCHAR(80) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    color VARCHAR(32),
    position_index INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    deleted_by_user_id UUID
);

CREATE TABLE IF NOT EXISTS calendar_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_user_id UUID NOT NULL,
    title VARCHAR(220) NOT NULL,
    notes VARCHAR(5000),
    location VARCHAR(240),
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ,
    all_day BOOLEAN NOT NULL DEFAULT FALSE,
    time_zone VARCHAR(80) NOT NULL DEFAULT 'UTC',
    category_key VARCHAR(80),
    color VARCHAR(32),
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    reminder_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    deleted_by_user_id UUID
);

CREATE INDEX IF NOT EXISTS idx_calendar_categories_owner ON calendar_categories(owner_user_id);
CREATE INDEX IF NOT EXISTS idx_calendar_categories_key ON calendar_categories(category_key);
CREATE INDEX IF NOT EXISTS idx_calendar_categories_deleted_at ON calendar_categories(deleted_at);
CREATE INDEX IF NOT EXISTS idx_calendar_events_owner ON calendar_events(owner_user_id);
CREATE INDEX IF NOT EXISTS idx_calendar_events_start_at ON calendar_events(start_at);
CREATE INDEX IF NOT EXISTS idx_calendar_events_category_key ON calendar_events(category_key);
CREATE INDEX IF NOT EXISTS idx_calendar_events_status ON calendar_events(status);
CREATE INDEX IF NOT EXISTS idx_calendar_events_deleted_at ON calendar_events(deleted_at);
