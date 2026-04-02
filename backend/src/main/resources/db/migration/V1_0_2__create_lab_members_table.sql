CREATE TABLE lab_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lab_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    status VARCHAR(50) NOT NULL,
    CONSTRAINT uk_lab_members_lab_user UNIQUE (lab_id, user_id),
    CONSTRAINT fk_lab_members_lab FOREIGN KEY (lab_id) REFERENCES labs(id),
    CONSTRAINT fk_lab_members_user FOREIGN KEY (user_id) REFERENCES users(id)
);
