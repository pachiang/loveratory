CREATE TABLE lab_invitations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lab_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL,
    invited_by UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    accepted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_lab_invitations_token UNIQUE (token),
    CONSTRAINT fk_lab_invitations_lab FOREIGN KEY (lab_id) REFERENCES labs(id),
    CONSTRAINT fk_lab_invitations_invited_by FOREIGN KEY (invited_by) REFERENCES users(id)
);
