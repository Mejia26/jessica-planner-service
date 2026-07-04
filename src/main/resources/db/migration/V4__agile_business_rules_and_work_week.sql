ALTER TABLE boards ADD COLUMN IF NOT EXISTS workflow_mode VARCHAR(20) NOT NULL DEFAULT 'OPEN';

ALTER TABLE board_columns ADD COLUMN IF NOT EXISTS status_category VARCHAR(20) NOT NULL DEFAULT 'TO_DO';
UPDATE board_columns
SET status_category = CASE
    WHEN UPPER(status_key) IN ('DONE', 'CLOSED', 'RESOLVED') THEN 'DONE'
    WHEN UPPER(status_key) IN ('IN_PROGRESS', 'IN REVIEW', 'REVIEW', 'QA', 'TESTING') THEN 'IN_PROGRESS'
    ELSE 'TO_DO'
END
WHERE status_category IS NULL OR status_category = 'TO_DO';

ALTER TABLE sprints ADD COLUMN IF NOT EXISTS started_at TIMESTAMPTZ;
ALTER TABLE sprints ADD COLUMN IF NOT EXISTS completed_at TIMESTAMPTZ;
ALTER TABLE sprints ADD COLUMN IF NOT EXISTS commitment_baseline_points INTEGER NOT NULL DEFAULT 0;

ALTER TABLE issues ADD COLUMN IF NOT EXISTS resolution VARCHAR(30);
ALTER TABLE issues ADD COLUMN IF NOT EXISTS class_of_service VARCHAR(30) NOT NULL DEFAULT 'STANDARD';
ALTER TABLE issues ADD COLUMN IF NOT EXISTS sprint_scope_added BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_board_columns_status_category ON board_columns(status_category);
CREATE INDEX IF NOT EXISTS idx_boards_workflow_mode ON boards(workflow_mode);
CREATE INDEX IF NOT EXISTS idx_issues_resolution ON issues(resolution);
CREATE INDEX IF NOT EXISTS idx_issues_class_of_service ON issues(class_of_service);
CREATE INDEX IF NOT EXISTS idx_issues_parent_issue_id ON issues(parent_issue_id);

CREATE TABLE IF NOT EXISTS work_week_meetings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meeting_date DATE NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    purpose VARCHAR(220) NOT NULL,
    description VARCHAR(2000),
    created_by_user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    deleted_by_user_id UUID
);

CREATE TABLE IF NOT EXISTS work_week_meeting_participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meeting_id UUID NOT NULL REFERENCES work_week_meetings(id),
    display_name VARCHAR(160) NOT NULL,
    role_label VARCHAR(120),
    external_reference VARCHAR(160),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    deleted_by_user_id UUID
);

CREATE INDEX IF NOT EXISTS idx_work_week_meetings_date ON work_week_meetings(meeting_date);
CREATE INDEX IF NOT EXISTS idx_work_week_meetings_day ON work_week_meetings(day_of_week);
CREATE INDEX IF NOT EXISTS idx_work_week_meetings_deleted_at ON work_week_meetings(deleted_at);
CREATE INDEX IF NOT EXISTS idx_work_week_participants_meeting ON work_week_meeting_participants(meeting_id);
