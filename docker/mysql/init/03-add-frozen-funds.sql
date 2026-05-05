-- 冻结资金表：支付时资金暂存此处，确认收货后释放给商家
USE `elm2`;

CREATE TABLE IF NOT EXISTS `frozen_funds` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `orderId` INT NOT NULL,
  `userId` VARCHAR(50) NOT NULL,
  `businessUserId` VARCHAR(50) NOT NULL,
  `amount` DECIMAL(12,2) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `createTime` VARCHAR(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_frozen_funds_order` (`orderId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
