ALTER TABLE projects ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS deleted_by_user_id UUID;

ALTER TABLE boards ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
ALTER TABLE boards ADD COLUMN IF NOT EXISTS deleted_by_user_id UUID;

ALTER TABLE board_columns ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
ALTER TABLE board_columns ADD COLUMN IF NOT EXISTS deleted_by_user_id UUID;

ALTER TABLE sprints ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
ALTER TABLE sprints ADD COLUMN IF NOT EXISTS deleted_by_user_id UUID;

ALTER TABLE issues ADD COLUMN IF NOT EXISTS category_key VARCHAR(80);
ALTER TABLE issues ADD COLUMN IF NOT EXISTS component_key VARCHAR(80);
ALTER TABLE issues ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
ALTER TABLE issues ADD COLUMN IF NOT EXISTS deleted_by_user_id UUID;

ALTER TABLE issue_comments ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
ALTER TABLE issue_comments ADD COLUMN IF NOT EXISTS deleted_by_user_id UUID;

ALTER TABLE issue_attachments ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
ALTER TABLE issue_attachments ADD COLUMN IF NOT EXISTS deleted_by_user_id UUID;

ALTER TABLE board_columns DROP CONSTRAINT IF EXISTS uq_board_columns_board_status;

CREATE TABLE IF NOT EXISTS issue_labels (
    issue_id UUID NOT NULL REFERENCES issues(id),
    label_key VARCHAR(80) NOT NULL,
    PRIMARY KEY (issue_id, label_key)
);

CREATE TABLE IF NOT EXISTS project_options (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id),
    type VARCHAR(40) NOT NULL,
    option_key VARCHAR(80) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    color VARCHAR(32),
    position_index INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    deleted_by_user_id UUID
);

CREATE INDEX IF NOT EXISTS idx_projects_deleted_at ON projects(deleted_at);
CREATE INDEX IF NOT EXISTS idx_boards_deleted_at ON boards(deleted_at);
CREATE INDEX IF NOT EXISTS idx_board_columns_deleted_at ON board_columns(deleted_at);
CREATE INDEX IF NOT EXISTS idx_sprints_deleted_at ON sprints(deleted_at);
CREATE INDEX IF NOT EXISTS idx_issues_deleted_at ON issues(deleted_at);
CREATE INDEX IF NOT EXISTS idx_issues_category_key ON issues(category_key);
CREATE INDEX IF NOT EXISTS idx_issues_component_key ON issues(component_key);
CREATE INDEX IF NOT EXISTS idx_issue_labels_label_key ON issue_labels(label_key);
CREATE INDEX IF NOT EXISTS idx_project_options_project_id ON project_options(project_id);
CREATE INDEX IF NOT EXISTS idx_project_options_type ON project_options(type);
CREATE INDEX IF NOT EXISTS idx_project_options_key ON project_options(option_key);
