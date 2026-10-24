SELECT
    /*%expand*/*
FROM
    scores
WHERE
    user_id = /* userId */1
ORDER BY
    created_at DESC
    , id DESC
