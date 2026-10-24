SELECT
    last_read_at
FROM
    user_notification_states
WHERE
    user_id = /* userId */1
    AND category = /* category */'announce'
