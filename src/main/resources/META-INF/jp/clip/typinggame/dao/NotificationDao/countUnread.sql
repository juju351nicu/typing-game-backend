SELECT
    COUNT(*)
FROM
    notifications
WHERE
    category = /* category */'announce'
    AND created_at >= /* since */'2026-01-01 00:00:00'
    AND (user_id = /* userId */1 OR user_id IS NULL)
    -- 最終確認日時より後に作成された通知を未読とします。
    AND created_at > /* lastReadAt */'2026-01-01 00:00:00'
