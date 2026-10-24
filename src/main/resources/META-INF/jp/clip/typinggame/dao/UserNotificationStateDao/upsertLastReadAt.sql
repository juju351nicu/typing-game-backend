INSERT INTO user_notification_states (
    user_id
    , category
    , last_read_at
    , created_at
    , updated_at
)
VALUES (
    /* userId */1
    , /* category */'announce'
    , /* readAt */'2026-01-01 00:00:00'
    , /* readAt */'2026-01-01 00:00:00'
    , /* readAt */'2026-01-01 00:00:00'
)
ON DUPLICATE KEY UPDATE
    last_read_at = /* readAt */'2026-01-01 00:00:00'
    , updated_at = /* readAt */'2026-01-01 00:00:00'
