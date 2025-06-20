-- 为resource表添加source字段
-- 执行此脚本前请备份数据库

-- 添加source字段
ALTER TABLE resource ADD COLUMN source INT DEFAULT NULL COMMENT '数据来源：1-Excel导入，2-爬虫，3-手动添加等';

-- 为现有数据设置默认值（假设现有数据为手动添加）
UPDATE resource SET source = 3 WHERE source IS NULL;

-- 添加索引以提高查询性能
CREATE INDEX idx_resource_source ON resource(source);

-- 查看表结构确认
DESCRIBE resource;
