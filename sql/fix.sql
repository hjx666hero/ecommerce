-- ============================================
-- 数据库修复脚本
-- 修复 admin 密码 BCrypt hash（原 hash 格式错误导致无法登录）
-- 请在 MySQL 中执行此脚本
-- ============================================

USE ecommerce;

-- 修复 admin 管理员密码（密码: admin123）
UPDATE admin_user
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE username = 'admin';

-- 验证修复结果
SELECT id, username, nickname, role, status FROM admin_user WHERE username = 'admin';
