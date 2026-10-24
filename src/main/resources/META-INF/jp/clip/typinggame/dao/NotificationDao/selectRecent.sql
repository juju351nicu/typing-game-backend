SELECT
    /*%expand*/*
FROM
    notifications
ORDER BY
    created_at DESC
    , id DESC
LIMIT /* limit */50
