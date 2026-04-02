CREATE TABLE project_investigators (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    added_by UUID NOT NULL,
    added_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    status VARCHAR(50) NOT NULL,
    CONSTRAINT uk_project_investigators UNIQUE (project_id, user_id),
    CONSTRAINT fk_pi_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_pi_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_pi_added_by FOREIGN KEY (added_by) REFERENCES users(id)
);
