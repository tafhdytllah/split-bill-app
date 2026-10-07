-- ============================================================
-- MEDIUM PERFORMANCE DATASET
-- 1 group
-- 20 participants
-- 100 expenses
-- 2.000 expense splits
-- ============================================================

-- GROUP
INSERT INTO groups (id, name, created_at)
VALUES (
           '00000000-0000-0000-0000-000000000002',
           'Performance Medium',
           CURRENT_TIMESTAMP
       );

-- 20 PARTICIPANTS
INSERT INTO participants (id, group_id, name, created_at)
SELECT
    md5('medium-participant-' || n)::uuid,
    '00000000-0000-0000-0000-000000000002',
    'Participant ' || LPAD(n::text, 2, '0'),
    CURRENT_TIMESTAMP
FROM generate_series(1, 20) AS n;

-- 100 EXPENSES
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
    md5('medium-expense-' || n)::uuid,
    '00000000-0000-0000-0000-000000000002',
    (
        SELECT id
        FROM participants
        WHERE group_id = '00000000-0000-0000-0000-000000000002'
        ORDER BY id
        OFFSET ((n - 1) % 20)
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
FROM generate_series(1, 100) AS n;

-- 2.000 EXPENSE SPLITS
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
    e.amount / 20
FROM expenses e
         CROSS JOIN participants p
WHERE e.group_id = '00000000-0000-0000-0000-000000000002'
  AND p.group_id = '00000000-0000-0000-0000-000000000002';