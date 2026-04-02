CREATE TABLE registrations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    time_slot_id UUID NOT NULL,
    participant_email VARCHAR(255) NOT NULL,
    participant_name VARCHAR(100),
    participant_phone VARCHAR(50),
    participant_student_id VARCHAR(50),
    participant_age INTEGER,
    participant_gender VARCHAR(50),
    participant_dominant_hand VARCHAR(50),
    participant_notes TEXT,
    cancel_token VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    registered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    cancelled_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_registrations_cancel_token UNIQUE (cancel_token),
    CONSTRAINT fk_registrations_slot FOREIGN KEY (time_slot_id) REFERENCES time_slots(id)
);
CREATE INDEX idx_registrations_slot_status ON registrations(time_slot_id, status);
CREATE INDEX idx_registrations_email ON registrations(participant_email);
