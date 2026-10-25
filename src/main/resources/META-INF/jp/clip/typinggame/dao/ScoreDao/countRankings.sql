SELECT
    COUNT(*)
FROM
    scores
-- 条件はselectRankingsPage.sqlとそろえ、総件数とページ内の行が食い違わないようにします。
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
