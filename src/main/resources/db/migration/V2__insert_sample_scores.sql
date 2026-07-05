INSERT IGNORE INTO scores (
  id,
  time,
  score,
  mode,
  game_rule,
  time_limit_seconds,
  wpm,
  accuracy,
  miss_count,
  correct_character_count,
  created_at
) VALUES
  (1, '00:00:28.00', 12, 2, 'timeAttack', 60, 32, 96, 2, 80, CURRENT_TIMESTAMP),
  (2, '00:01:10.00', 18, 1, 'normal', NULL, 24, 94, 4, 92, CURRENT_TIMESTAMP);
