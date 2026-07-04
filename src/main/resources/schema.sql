CREATE TABLE IF NOT EXISTS scores (
  id BIGINT NOT NULL AUTO_INCREMENT,
  time VARCHAR(20) NOT NULL,
  score INT NOT NULL,
  mode INT NOT NULL,
  game_rule VARCHAR(20) NOT NULL DEFAULT 'normal',
  time_limit_seconds INT NULL,
  wpm INT NULL,
  accuracy INT NULL,
  miss_count INT NULL,
  correct_character_count INT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_scores_created_at (created_at),
  INDEX idx_scores_ranking (game_rule, time_limit_seconds, score, time)
);
