CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  login_email VARCHAR(255) NOT NULL,
  password VARCHAR(100) NOT NULL,
  last_login_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_login_email (login_email)
);

CREATE TABLE IF NOT EXISTS scores (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NULL,
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
  INDEX idx_scores_ranking (game_rule, time_limit_seconds, score, time),
  INDEX idx_scores_user_created_at (user_id, created_at),
  CONSTRAINT fk_scores_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);
