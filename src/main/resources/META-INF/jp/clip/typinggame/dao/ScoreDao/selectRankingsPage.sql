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
-- 同点・同タイムでもページ間で行が重複・欠落しないよう、最後にidで並びを一意にします。
ORDER BY
    score DESC
    , time ASC
    , created_at DESC
    , id DESC
LIMIT /* limit */10
OFFSET /* offset */0
