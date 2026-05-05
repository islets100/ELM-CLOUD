-- 积分系统数据库初始化脚本
-- 适用于 MySQL 数据库

-- 积分表
CREATE TABLE IF NOT EXISTS integral (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '积分记录ID',
    user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
    amount INT NOT NULL COMMENT '积分金额',
    type VARCHAR(20) NOT NULL COMMENT '积分类型（INCREASE-增加, DECREASE-减少）',
    status VARCHAR(20) NOT NULL COMMENT '积分状态（AVAILABLE-可用, USED-已使用, EXPIRED-已过期）',
    channel VARCHAR(50) NOT NULL COMMENT '积分渠道（获得或消费）',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    business_id BIGINT COMMENT '关联业务ID',
    description VARCHAR(255) COMMENT '业务描述',
    remaining_amount INT COMMENT '剩余积分（用于部分消费）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分表';

-- 签到记录表
CREATE TABLE IF NOT EXISTS sign_in_records (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '签到记录ID',
    user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
    sign_date DATE NOT NULL COMMENT '签到日期',
    consecutive_days INT NOT NULL DEFAULT 1 COMMENT '连续签到天数',
    points_awarded INT NOT NULL COMMENT '获得的积分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_date (user_id, sign_date),
    INDEX idx_user_id (user_id),
    INDEX idx_sign_date (sign_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- 生日积分记录表
CREATE TABLE IF NOT EXISTS birthday_integral_records (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '生日积分记录ID',
    user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    monthly_earned BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否获得月度生日积分',
    birthday_earned BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否获得生日当天积分',
    monthly_points INT DEFAULT 0 COMMENT '获得的月度积分',
    birthday_points INT DEFAULT 0 COMMENT '获得的生日当天积分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_date (user_id, record_date),
    INDEX idx_user_id (user_id),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生日积分记录表';
