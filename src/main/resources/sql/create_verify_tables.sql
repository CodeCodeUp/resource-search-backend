-- 滑动验证系统数据库表结构

-- 验证挑战表
CREATE TABLE verify_challenges (
  id VARCHAR(64) PRIMARY KEY DEFAULT (UUID()),
  challenge_id VARCHAR(128) UNIQUE NOT NULL,
  background_image TEXT,
  puzzle_position JSON,
  tolerance INT DEFAULT 5,
  device_fingerprint VARCHAR(64),
  user_agent TEXT,
  ip_address VARCHAR(45),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP,
  used_at TIMESTAMP NULL,
  status ENUM('pending', 'verified', 'expired', 'failed') DEFAULT 'pending',
  INDEX idx_challenge_id (challenge_id),
  INDEX idx_expires_at (expires_at),
  INDEX idx_device_fingerprint (device_fingerprint),
  INDEX idx_ip_address (ip_address),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证挑战表';

-- 验证会话表
CREATE TABLE verify_sessions (
  id VARCHAR(64) PRIMARY KEY DEFAULT (UUID()),
  session_id VARCHAR(128) UNIQUE NOT NULL,
  challenge_id VARCHAR(128),
  resource_id VARCHAR(64),
  access_token VARCHAR(256),
  device_fingerprint VARCHAR(64),
  user_agent TEXT,
  ip_address VARCHAR(45),
  verified_at TIMESTAMP,
  expires_at TIMESTAMP,
  last_access_at TIMESTAMP,
  access_count INT DEFAULT 0,
  status ENUM('active', 'expired', 'revoked') DEFAULT 'active',
  INDEX idx_session_id (session_id),
  INDEX idx_access_token (access_token),
  INDEX idx_resource_id (resource_id),
  INDEX idx_expires_at (expires_at),
  INDEX idx_device_fingerprint (device_fingerprint),
  INDEX idx_ip_address (ip_address),
  INDEX idx_status (status),
  FOREIGN KEY (challenge_id) REFERENCES verify_challenges(challenge_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证会话表';

-- 访问日志表
CREATE TABLE verify_access_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  session_id VARCHAR(128),
  resource_id VARCHAR(64),
  action VARCHAR(32),
  ip_address VARCHAR(45),
  user_agent TEXT,
  device_fingerprint VARCHAR(64),
  success BOOLEAN,
  error_message TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_session_id (session_id),
  INDEX idx_resource_id (resource_id),
  INDEX idx_created_at (created_at),
  INDEX idx_ip_address (ip_address),
  INDEX idx_device_fingerprint (device_fingerprint),
  INDEX idx_success (success),
  INDEX idx_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访问日志表';

-- 创建索引以优化查询性能
CREATE INDEX idx_challenges_device_time ON verify_challenges(device_fingerprint, created_at);
CREATE INDEX idx_challenges_ip_time ON verify_challenges(ip_address, created_at);
CREATE INDEX idx_challenges_status_time ON verify_challenges(status, created_at);

CREATE INDEX idx_sessions_device_time ON verify_sessions(device_fingerprint, verified_at);
CREATE INDEX idx_sessions_resource_status ON verify_sessions(resource_id, status);

CREATE INDEX idx_logs_ip_time ON verify_access_logs(ip_address, created_at);
CREATE INDEX idx_logs_device_time ON verify_access_logs(device_fingerprint, created_at);
CREATE INDEX idx_logs_success_time ON verify_access_logs(success, created_at);

-- 创建定时清理过期数据的事件（可选）
-- DELIMITER $$
-- CREATE EVENT IF NOT EXISTS cleanup_expired_verify_data
-- ON SCHEDULE EVERY 1 HOUR
-- DO
-- BEGIN
--   -- 删除过期的挑战（超过24小时）
--   DELETE FROM verify_challenges 
--   WHERE created_at < DATE_SUB(NOW(), INTERVAL 24 HOUR);
--   
--   -- 删除过期的会话（超过24小时）
--   DELETE FROM verify_sessions 
--   WHERE expires_at < DATE_SUB(NOW(), INTERVAL 24 HOUR);
--   
--   -- 删除旧的访问日志（超过30天）
--   DELETE FROM verify_access_logs 
--   WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
-- END$$
-- DELIMITER ;

-- 插入一些测试数据（可选）
-- INSERT INTO verify_challenges (challenge_id, background_image, puzzle_position, device_fingerprint, user_agent, ip_address, expires_at) 
-- VALUES 
-- ('challenge_test_001', 'test_image_data', '{"x": 150, "y": 75}', 'test_device_001', 'Mozilla/5.0', '127.0.0.1', DATE_ADD(NOW(), INTERVAL 5 MINUTE));
