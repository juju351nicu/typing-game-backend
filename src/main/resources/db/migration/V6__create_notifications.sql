CREATE TABLE IF NOT EXISTS notifications (
  id BIGINT NOT NULL AUTO_INCREMENT,
  category VARCHAR(20) NOT NULL,
  -- NULLのときは全ユーザー宛て、値があるときはそのユーザーだけに届けます。
  user_id BIGINT NULL,
  title VARCHAR(100) NOT NULL,
  body VARCHAR(500) NOT NULL,
  -- 通知から遷移させる画面のパスです。NULLなら遷移しません。
  link_path VARCHAR(200) NULL,
  created_by BIGINT NOT NULL,
  -- 未読判定で最終確認日時と比較するため、秒未満まで保持します。
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  INDEX idx_notifications_category_created_at (category, created_at),
  INDEX idx_notifications_user_created_at (user_id, created_at),
  CONSTRAINT fk_notifications_user_id FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_notifications_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

-- 既読は通知×ユーザーではなく、ユーザーと通知種別ごとの最終確認日時1行で管理します。
CREATE TABLE IF NOT EXISTS user_notification_states (
  user_id BIGINT NOT NULL,
  category VARCHAR(20) NOT NULL,
  last_read_at TIMESTAMP(6) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, category),
  CONSTRAINT fk_user_notification_states_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);
