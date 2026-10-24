SELECT
    /*%expand*/*
FROM
    notifications
WHERE
    category = /* category */'announce'
    AND created_at >= /* since */'2026-01-01 00:00:00'
    -- 自分宛て、またはuser_idがNULLの全体宛てだけを返します。
    AND (user_id = /* userId */1 OR user_id IS NULL)
ORDER BY
    created_at DESC
    , id DESC
LIMIT /* limit */50
