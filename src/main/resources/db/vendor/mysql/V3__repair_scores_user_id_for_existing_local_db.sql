SET @column_exists := (
  SELECT COUNT(*)
    FROM information_schema.columns
   WHERE table_schema = DATABASE()
     AND table_name = 'scores'
     AND column_name = 'user_id'
);

SET @ddl := IF(
  @column_exists = 0,
  'ALTER TABLE scores ADD COLUMN user_id BIGINT NULL',
  'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
  SELECT COUNT(*)
    FROM information_schema.statistics
   WHERE table_schema = DATABASE()
     AND table_name = 'scores'
     AND index_name = 'idx_scores_user_created_at'
);

SET @ddl := IF(
  @index_exists = 0,
  'ALTER TABLE scores ADD INDEX idx_scores_user_created_at (user_id, created_at)',
  'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @constraint_exists := (
  SELECT COUNT(*)
    FROM information_schema.referential_constraints
   WHERE constraint_schema = DATABASE()
     AND constraint_name = 'fk_scores_user_id'
);

SET @ddl := IF(
  @constraint_exists = 0,
  'ALTER TABLE scores ADD CONSTRAINT fk_scores_user_id FOREIGN KEY (user_id) REFERENCES users(id)',
  'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
