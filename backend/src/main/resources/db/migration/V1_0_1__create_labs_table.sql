CREATE TABLE labs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    applied_by UUID NOT NULL,
    reviewed_by UUID,
    review_note TEXT,
    approved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_labs_code UNIQUE (code),
    CONSTRAINT fk_labs_applied_by FOREIGN KEY (applied_by) REFERENCES users(id),
    CONSTRAINT fk_labs_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users(id)
);
