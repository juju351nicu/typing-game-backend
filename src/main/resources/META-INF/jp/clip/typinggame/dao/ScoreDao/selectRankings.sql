SELECT
    /*%expand*/*
FROM
    scores
-- 条件がすべてnullの場合、DomaがWHEREを、先頭の条件がnullの場合はANDを自動で取り除きます。
WHERE
/*%if mode != null */
    mode = /* mode */2
/*%end*/
/*%if gameRule != null */
    AND game_rule = /* gameRule */'timeAttack'
/*%end*/
/*%if timeLimitSeconds != null */
    AND time_limit_seconds = /* timeLimitSeconds */60
/*%end*/
ORDER BY
    score DESC
    , time ASC
    , created_at DESC
LIMIT /* limit */20
