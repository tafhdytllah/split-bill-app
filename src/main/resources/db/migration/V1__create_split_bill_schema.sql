CREATE TABLE groups
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(255)             NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE participants
(
    id         UUID PRIMARY KEY,
    group_id   UUID                     NOT NULL,
    name       VARCHAR(255)             NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_participant_group
        FOREIGN KEY (group_id)
            REFERENCES groups (id),

    CONSTRAINT uq_participant_name_per_group
        UNIQUE (group_id, name),

    CONSTRAINT uq_participant_group_id
        UNIQUE (group_id, id)
);

CREATE TABLE expenses
(
    id         UUID PRIMARY KEY,
    group_id   UUID                     NOT NULL,
    paid_by    UUID                     NOT NULL,
    amount     NUMERIC(19, 2)           NOT NULL,
    category   VARCHAR(50)              NOT NULL,
    split_type VARCHAR(50)              NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_expense_group
        FOREIGN KEY (group_id)
            REFERENCES groups (id),

    CONSTRAINT fk_expense_paid_by
        FOREIGN KEY (group_id, paid_by)
            REFERENCES participants (group_id, id),

    CONSTRAINT chk_expense_amount_positive
        CHECK (amount > 0)
);

CREATE TABLE expense_splits
(
    id             UUID PRIMARY KEY,
    expense_id     UUID           NOT NULL,
    participant_id UUID           NOT NULL,
    amount         NUMERIC(19, 2) NOT NULL,

    CONSTRAINT fk_split_expense
        FOREIGN KEY (expense_id)
            REFERENCES expenses (id),

    CONSTRAINT fk_split_participant
        FOREIGN KEY (participant_id)
            REFERENCES participants (id),

    CONSTRAINT uq_expense_participant
        UNIQUE (expense_id, participant_id),

    CONSTRAINT chk_split_amount_non_negative
        CHECK (amount >= 0)
);

CREATE TABLE payments
(
    id                  UUID PRIMARY KEY,
    group_id            UUID                     NOT NULL,
    from_participant_id UUID                     NOT NULL,
    to_participant_id   UUID                     NOT NULL,
    amount              NUMERIC(19, 2)           NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_payment_group
        FOREIGN KEY (group_id)
            REFERENCES groups (id),

    CONSTRAINT fk_payment_from
        FOREIGN KEY (group_id, from_participant_id)
            REFERENCES participants (group_id, id),

    CONSTRAINT fk_payment_to
        FOREIGN KEY (group_id, to_participant_id)
            REFERENCES participants (group_id, id),

    CONSTRAINT chk_payment_amount_positive
        CHECK (amount > 0),

    CONSTRAINT chk_payment_different_participants
        CHECK (from_participant_id <> to_participant_id)
);

CREATE TABLE audit_logs
(
    id          UUID PRIMARY KEY,
    group_id    UUID                     NOT NULL,
    action      VARCHAR(50)              NOT NULL,
    entity_type VARCHAR(50)              NOT NULL,
    entity_id   UUID                     NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_audit_group
        FOREIGN KEY (group_id)
            REFERENCES groups (id)
);

CREATE TABLE idempotencies
(
    id  UUID PRIMARY KEY,
    scope VARCHAR(100) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    response_code INTEGER,
    response_body TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uk_idempotency_scope_key
        UNIQUE (scope, idempotency_key)
);

-- Indexes based on query patterns

CREATE INDEX idx_expenses_group_id
    ON expenses (group_id);

CREATE INDEX idx_payments_group_id
    ON payments (group_id);

CREATE INDEX idx_audit_logs_group_id
    ON audit_logs (group_id);