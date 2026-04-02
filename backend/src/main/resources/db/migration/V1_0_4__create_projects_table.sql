CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lab_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    created_by UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_projects_lab FOREIGN KEY (lab_id) REFERENCES labs(id),
    CONSTRAINT fk_projects_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);
