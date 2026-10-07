-- ============================================================
-- SMALL PERFORMANCE DATASET
-- 1 group
-- 10 participants
-- 20 expenses
-- 200 expense splits
-- ============================================================

-- Group
INSERT INTO groups (
    id,
    name,
    created_at
)
VALUES (
           '00000000-0000-0000-0000-000000000001',
           'Performance Small',
           CURRENT_TIMESTAMP
       );

-- 10 Participants
INSERT INTO participants (
    id,
    group_id,
    name,
    created_at
)
SELECT
    md5('small-participant-' || n)::uuid,
    '00000000-0000-0000-0000-000000000001',
    'Participant ' || LPAD(n::text, 2, '0'),
    CURRENT_TIMESTAMP
FROM generate_series(1, 10) AS n;

-- 20 Expenses
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
    md5('small-expense-' || n)::uuid,
    '00000000-0000-0000-0000-000000000001',
    (
        SELECT id
        FROM participants
        WHERE group_id = '00000000-0000-0000-0000-000000000001'
        ORDER BY name
        OFFSET ((n - 1) % 10)
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
FROM generate_series(1, 20) AS n;

-- 200 Expense Splits
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
    e.amount / 10
FROM expenses e
         CROSS JOIN participants p
WHERE e.group_id = '00000000-0000-0000-0000-000000000001'
  AND p.group_id = '00000000-0000-0000-0000-000000000001';