-- ローカル開発専用のシードデータです。
-- application.yml（ローカル）のflyway.locationsにだけ含め、本番（application-prod.yml）とテストでは読み込みません。
-- Repeatable migration（R__）のため、このファイルを変更すると次回起動時に再実行されます。何度実行しても同じ状態になるように書きます。
--
-- ログイン情報（ローカル専用）
--   管理者:       local-admin@example.com / admin-password
--   一般ユーザー: local-user@example.com  / user-password

INSERT INTO users (login_email, password, role)
VALUES
  ('local-admin@example.com', '$2a$10$wgDR7p2Mu8l3aztghyiQ1edVxibqxj68oSccnXYlX.7z0inT1W9j.', 'admin'),
  ('local-user@example.com', '$2a$10$52vPPngUPMpjH61Hbjy5.O0vLvDRECmMqeAWIWtQY.OyYdFjof9ea', 'user')
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  role = VALUES(role);

-- 全体ランキングのページ送り（LIMIT / OFFSET）を確認できるよう、2ユーザー分で24件のスコアを入れます。
-- IDは手動で登録したデータと重ならないよう1001から使います。
INSERT IGNORE INTO scores (
  id,
  user_id,
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
)
SELECT
  seed.id,
  users.id,
  seed.time,
  seed.score,
  seed.mode,
  seed.game_rule,
  seed.time_limit_seconds,
  seed.wpm,
  seed.accuracy,
  seed.miss_count,
  seed.correct_character_count,
  seed.created_at
FROM (
  VALUES
    ROW(1001, 'local-admin@example.com', '00:00:20.00', 3, 0, 'timeAttack', 30, 18, 82, 0, 52, '2026-10-01 09:00:00'),
    ROW(1002, 'local-user@example.com', '00:00:31.00', 10, 0, 'normal', NULL, 23, 85, 2, 80, '2026-10-02 10:13:00'),
    ROW(1003, 'local-admin@example.com', '00:00:42.00', 17, 1, 'normal', NULL, 28, 88, 4, 108, '2026-10-03 11:26:00'),
    ROW(1004, 'local-user@example.com', '00:00:53.00', 24, 1, 'timeAttack', 60, 33, 91, 6, 136, '2026-10-04 12:39:00'),
    ROW(1005, 'local-admin@example.com', '00:01:04.00', 9, 2, 'normal', NULL, 38, 94, 1, 76, '2026-10-05 13:52:00'),
    ROW(1006, 'local-user@example.com', '00:01:15.00', 16, 2, 'normal', NULL, 43, 97, 3, 104, '2026-10-06 14:05:00'),
    ROW(1007, 'local-admin@example.com', '00:00:26.00', 23, 0, 'timeAttack', 90, 18, 82, 5, 132, '2026-10-07 15:18:00'),
    ROW(1008, 'local-user@example.com', '00:00:37.00', 8, 0, 'normal', NULL, 23, 85, 0, 72, '2026-10-08 16:31:00'),
    ROW(1009, 'local-admin@example.com', '00:00:48.00', 15, 1, 'normal', NULL, 28, 88, 2, 100, '2026-10-09 17:44:00'),
    ROW(1010, 'local-user@example.com', '00:00:59.00', 22, 1, 'timeAttack', 30, 33, 91, 4, 128, '2026-10-10 18:57:00'),
    ROW(1011, 'local-admin@example.com', '00:01:10.00', 7, 2, 'normal', NULL, 38, 94, 6, 68, '2026-10-11 19:10:00'),
    ROW(1012, 'local-user@example.com', '00:00:21.00', 14, 2, 'normal', NULL, 43, 97, 1, 96, '2026-10-12 20:23:00'),
    ROW(1013, 'local-admin@example.com', '00:00:32.00', 21, 0, 'timeAttack', 60, 18, 82, 3, 124, '2026-10-13 09:36:00'),
    ROW(1014, 'local-user@example.com', '00:00:43.00', 6, 0, 'normal', NULL, 23, 85, 5, 64, '2026-10-14 10:49:00'),
    ROW(1015, 'local-admin@example.com', '00:00:54.00', 13, 1, 'normal', NULL, 28, 88, 0, 92, '2026-10-15 11:02:00'),
    ROW(1016, 'local-user@example.com', '00:01:05.00', 20, 1, 'timeAttack', 90, 33, 91, 2, 120, '2026-10-16 12:15:00'),
    ROW(1017, 'local-admin@example.com', '00:01:16.00', 5, 2, 'normal', NULL, 38, 94, 4, 60, '2026-10-17 13:28:00'),
    ROW(1018, 'local-user@example.com', '00:00:27.00', 12, 2, 'normal', NULL, 43, 97, 6, 88, '2026-10-18 14:41:00'),
    ROW(1019, 'local-admin@example.com', '00:00:38.00', 19, 0, 'timeAttack', 30, 18, 82, 1, 116, '2026-10-19 15:54:00'),
    ROW(1020, 'local-user@example.com', '00:00:49.00', 4, 0, 'normal', NULL, 23, 85, 3, 56, '2026-10-20 16:07:00'),
    ROW(1021, 'local-admin@example.com', '00:01:00.00', 11, 1, 'normal', NULL, 28, 88, 5, 84, '2026-10-21 17:20:00'),
    ROW(1022, 'local-user@example.com', '00:01:11.00', 18, 1, 'timeAttack', 60, 33, 91, 0, 112, '2026-10-22 18:33:00'),
    ROW(1023, 'local-admin@example.com', '00:00:22.00', 3, 2, 'normal', NULL, 38, 94, 2, 52, '2026-10-23 19:46:00'),
    ROW(1024, 'local-user@example.com', '00:00:33.00', 10, 2, 'normal', NULL, 43, 97, 4, 80, '2026-10-24 20:59:00')
) AS seed (
  id, login_email, time, score, mode, game_rule, time_limit_seconds,
  wpm, accuracy, miss_count, correct_character_count, created_at
)
INNER JOIN users ON users.login_email = seed.login_email;
