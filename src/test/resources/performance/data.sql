TRUNCATE TABLE
    expense_splits,
    expenses,
    payments,
    idempotencies,
    participants,
    groups
    RESTART IDENTITY
    CASCADE;

SELECT COUNT(*) FROM participants;
SELECT COUNT(*) FROM expenses;
SELECT COUNT(*) FROM expense_splits;

EXPLAIN (ANALYZE, BUFFERS)
SELECT
    e.id,
    e.amount,
    e.category,
    e.created_at,
    e.group_id,
    e.paid_by,
    e.split_type,
    s.expense_id,
    s.id,
    s.amount,
    s.participant_id
FROM expenses e
         LEFT JOIN expense_splits s
                   ON e.id = s.expense_id
WHERE e.group_id = 'GROUP_ID_LARGE';

-- ============================================================
-- SEED DATA
-- ============================================================

-- ============================================================
-- SEED DATA
-- Scenario: Weekend Hangout
-- ============================================================

-- ------------------------------------------------------------
-- Group
-- ------------------------------------------------------------

INSERT INTO groups (
    id,
    name,
    created_at
)
VALUES (
           '11111111-1111-1111-1111-111111111111',
           'Weekend Hangout',
           '2026-10-03T10:00:00Z'
       );


-- ------------------------------------------------------------
-- Participants
-- ------------------------------------------------------------

INSERT INTO participants (
    id,
    group_id,
    name,
    created_at
)
VALUES
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        '11111111-1111-1111-1111-111111111111',
        'Andi',
        '2026-10-03T10:01:00Z'
    ),
    (
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        '11111111-1111-1111-1111-111111111111',
        'Budi',
        '2026-10-03T10:01:00Z'
    ),
    (
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        '11111111-1111-1111-1111-111111111111',
        'Coki',
        '2026-10-03T10:01:00Z'
    ),
    (
        'dddddddd-dddd-dddd-dddd-dddddddddddd',
        '11111111-1111-1111-1111-111111111111',
        'Dinda',
        '2026-10-03T10:01:00Z'
    );


-- ============================================================
-- EXPENSE 1
-- Dinner
-- Andi pays Rp 400,000
-- Split equally among 4 participants
--
-- Andi  : 100,000
-- Budi  : 100,000
-- Coki  : 100,000
-- Dinda : 100,000
-- ============================================================

INSERT INTO expenses (
    id,
    group_id,
    paid_by,
    amount,
    category,
    split_type,
    created_at
)
VALUES (
           'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1',
           '11111111-1111-1111-1111-111111111111',
           'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
           400000.00,
           'FOOD',
           'EQUAL',
           '2026-10-03T10:10:00Z'
       );

INSERT INTO expense_splits (
    id,
    expense_id,
    participant_id,
    amount
)
VALUES
    (
        'e1111111-1111-1111-1111-111111111111',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        100000.00
    ),
    (
        'e1111111-1111-1111-1111-111111111112',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        100000.00
    ),
    (
        'e1111111-1111-1111-1111-111111111113',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        100000.00
    ),
    (
        'e1111111-1111-1111-1111-111111111114',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1',
        'dddddddd-dddd-dddd-dddd-dddddddddddd',
        100000.00
    );


-- ============================================================
-- EXPENSE 2
-- Transport
-- Budi pays Rp 160,000
-- Split equally among 4 participants
--
-- Each owes 40,000
-- ============================================================

INSERT INTO expenses (
    id,
    group_id,
    paid_by,
    amount,
    category,
    split_type,
    created_at
)
VALUES (
           'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2',
           '11111111-1111-1111-1111-111111111111',
           'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
           160000.00,
           'TRANSPORT',
           'EQUAL',
           '2026-10-03T11:00:00Z'
       );

INSERT INTO expense_splits (
    id,
    expense_id,
    participant_id,
    amount
)
VALUES
    (
        'e2222222-2222-2222-2222-222222222221',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        40000.00
    ),
    (
        'e2222222-2222-2222-2222-222222222222',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        40000.00
    ),
    (
        'e2222222-2222-2222-2222-222222222223',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        40000.00
    ),
    (
        'e2222222-2222-2222-2222-222222222224',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2',
        'dddddddd-dddd-dddd-dddd-dddddddddddd',
        40000.00
    );


-- ============================================================
-- EXPENSE 3
-- Dessert
-- Coki pays Rp 120,000
-- Split equally among 4 participants
--
-- Each owes 30,000
-- ============================================================

INSERT INTO expenses (
    id,
    group_id,
    paid_by,
    amount,
    category,
    split_type,
    created_at
)
VALUES (
           'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3',
           '11111111-1111-1111-1111-111111111111',
           'cccccccc-cccc-cccc-cccc-cccccccccccc',
           120000.00,
           'FOOD',
           'EQUAL',
           '2026-10-03T14:00:00Z'
       );

INSERT INTO expense_splits (
    id,
    expense_id,
    participant_id,
    amount
)
VALUES
    (
        'e3333333-3333-3333-3333-333333333331',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        30000.00
    ),
    (
        'e3333333-3333-3333-3333-333333333332',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        30000.00
    ),
    (
        'e3333333-3333-3333-3333-333333333333',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        30000.00
    ),
    (
        'e3333333-3333-3333-3333-333333333334',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3',
        'dddddddd-dddd-dddd-dddd-dddddddddddd',
        30000.00
    );


-- ============================================================
-- EXPENSE 4
-- Accommodation
-- Dinda pays Rp 320,000
-- Split equally among 4 participants
--
-- Each owes 80,000
-- ============================================================

INSERT INTO expenses (
    id,
    group_id,
    paid_by,
    amount,
    category,
    split_type,
    created_at
)
VALUES (
           'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee4',
           '11111111-1111-1111-1111-111111111111',
           'dddddddd-dddd-dddd-dddd-dddddddddddd',
           320000.00,
           'ACCOMMODATION',
           'EQUAL',
           '2026-10-03T20:00:00Z'
       );

INSERT INTO expense_splits (
    id,
    expense_id,
    participant_id,
    amount
)
VALUES
    (
        'e4444444-4444-4444-4444-444444444441',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee4',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        80000.00
    ),
    (
        'e4444444-4444-4444-4444-444444444442',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee4',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        80000.00
    ),
    (
        'e4444444-4444-4444-4444-444444444443',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee4',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        80000.00
    ),
    (
        'e4444444-4444-4444-4444-444444444444',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee4',
        'dddddddd-dddd-dddd-dddd-dddddddddddd',
        80000.00
    );