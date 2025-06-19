-- 数据库迁移脚本：为resource表新增resource_time字段

-- 检查字段是否已存在，如果不存在则添加
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'resource' 
     AND COLUMN_NAME = 'resource_time') = 0,
    'ALTER TABLE resource ADD COLUMN resource_time INT COMMENT "资源时间戳"',
    'SELECT "Column resource_time already exists" AS message'
));

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
