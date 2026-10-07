-- ============================================================
-- LARGE PERFORMANCE DATASET
-- 1 group
-- 30 participants
-- 500 expenses
-- 15.000 expense splits
-- ============================================================

-- GROUP
INSERT INTO groups (id, name, created_at)
VALUES (
           '00000000-0000-0000-0000-000000000003',
           'Performance Large',
           CURRENT_TIMESTAMP
       );

-- 30 PARTICIPANTS
INSERT INTO participants (
    id,
    group_id,
    name,
    created_at
)
SELECT
    md5('large-participant-' || n)::uuid,
    '00000000-0000-0000-0000-000000000003',
    'Participant ' || LPAD(n::text, 2, '0'),
    CURRENT_TIMESTAMP
FROM generate_series(1, 30) AS n;

-- 500 EXPENSES
INSERT INTO expenses (
    id,
    group_id,
    paid_by,
    amount,
    category,
    split_type,
    created_at
)
SELECT
    md5('large-expense-' || n)::uuid,
    '00000000-0000-0000-0000-000000000003',
    (
        SELECT id
        FROM participants
        WHERE group_id = '00000000-0000-0000-0000-000000000003'
        ORDER BY id
        OFFSET ((n - 1) % 30)
            LIMIT 1
    ),
    100000 + ((n - 1) * 10000),
    CASE (n - 1) % 3
        WHEN 0 THEN 'FOOD'
        WHEN 1 THEN 'TRANSPORT'
        ELSE 'ACCOMMODATION'
        END,
    'EQUAL',
    CURRENT_TIMESTAMP
FROM generate_series(1, 500) AS n;

-- 15.000 EXPENSE SPLITS
INSERT INTO expense_splits (
    id,
    expense_id,
    participant_id,
    amount
)
SELECT
    md5(e.id::text || p.id::text)::uuid,
    e.id,
    p.id,
    e.amount / 30
FROM expenses e
         CROSS JOIN participants p
WHERE e.group_id = '00000000-0000-0000-0000-000000000003'
  AND p.group_id = '00000000-0000-0000-0000-000000000003';