CREATE TABLE notifications
(
    id              UUID PRIMARY KEY,
    registration_id UUID         NOT NULL REFERENCES registrations (id),
    type            VARCHAR(50)  NOT NULL,
    channel         VARCHAR(20)  NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    scheduled_at    TIMESTAMPTZ  NOT NULL,
    sent_at         TIMESTAMPTZ,
    retry_count     INTEGER      NOT NULL DEFAULT 0,
    error_message   TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_status_scheduled_at
    ON notifications (status, scheduled_at);

CREATE INDEX idx_notifications_registration_id
    ON notifications (registration_id);
