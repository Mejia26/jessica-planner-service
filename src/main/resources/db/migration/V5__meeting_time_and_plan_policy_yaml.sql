ALTER TABLE work_week_meetings
    ADD COLUMN IF NOT EXISTS meeting_time TIME NOT NULL DEFAULT '09:00:00';

ALTER TABLE work_week_meetings
    ADD COLUMN IF NOT EXISTS time_zone VARCHAR(80) NOT NULL DEFAULT 'America/Santo_Domingo';

CREATE INDEX IF NOT EXISTS idx_work_week_meetings_time ON work_week_meetings(meeting_time);

CREATE TABLE IF NOT EXISTS features (
    feature_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feature_name VARCHAR(255) NOT NULL,
    description TEXT,
    is_deleted BOOLEAN DEFAULT FALSE,
    user_policy_id UUID,
    abbreviation_key VARCHAR(160),
    CONSTRAINT features_feature_name_key UNIQUE (feature_name)
);

CREATE TABLE IF NOT EXISTS actions (
    action_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    action_name VARCHAR(255) NOT NULL,
    description TEXT,
    is_deleted BOOLEAN DEFAULT FALSE,
    feature_id UUID REFERENCES features(feature_id),
    user_policy_id UUID,
    abbreviation_key VARCHAR(160),
    CONSTRAINT unique_user_policy_id UNIQUE (user_policy_id)
);

ALTER TABLE features
    ADD COLUMN IF NOT EXISTS abbreviation_key VARCHAR(160);

ALTER TABLE actions
    ADD COLUMN IF NOT EXISTS abbreviation_key VARCHAR(160);

CREATE INDEX IF NOT EXISTS idx_features_abbreviation_key ON features(abbreviation_key);
CREATE INDEX IF NOT EXISTS idx_actions_abbreviation_key ON actions(abbreviation_key);
CREATE INDEX IF NOT EXISTS idx_actions_feature_id ON actions(feature_id);
