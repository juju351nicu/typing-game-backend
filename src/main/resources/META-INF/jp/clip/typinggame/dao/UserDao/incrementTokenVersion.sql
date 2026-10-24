UPDATE
    users
SET
    token_version = token_version + 1
    , updated_at = /* updatedAt */'2026-01-01 00:00:00'
WHERE
    id = /* id */1
