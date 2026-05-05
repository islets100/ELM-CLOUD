-- 补充：为 Wallet 和 VIP 模块添加微服务架构下的缺失字段和表

USE `elm2`;

-- 1. 更新在 01-create-tables-if-not-exists.sql 中已创建但不完整的 VirtualWallet 表 (增加透支额度等支持)
ALTER TABLE `VirtualWallet` ADD COLUMN `creditLimit` DECIMAL(10,2) NOT NULL DEFAULT 0.00;
ALTER TABLE `VirtualWallet` ADD COLUMN `usedCreditLimit` DECIMAL(10,2) NOT NULL DEFAULT 0.00;
ALTER TABLE `VirtualWallet` ADD COLUMN `lastInterestTime` VARCHAR(50) DEFAULT NULL;

-- 2. 创建 VIP 记录表 (在微服务中完全缺失)
CREATE TABLE IF NOT EXISTS `vip_cards` (
  `cardId` INT NOT NULL AUTO_INCREMENT,
  `userId` VARCHAR(50) NOT NULL,
  `cardType` VARCHAR(20) NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `purchaseTime` VARCHAR(50) NOT NULL,
  `expiryTime` VARCHAR(50) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,  -- 1: 有效, 0: 无效
  PRIMARY KEY (`cardId`),
  KEY `idx_vip_cards_user` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 创建 overdraft_records (透支记录表)
CREATE TABLE IF NOT EXISTS `overdraft_records` (
  `recordId` INT NOT NULL AUTO_INCREMENT,
  `walletId` INT NOT NULL,
  `overdraftAmount` DECIMAL(10,2) NOT NULL,
  `repaidAmount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `accumulatedInterest` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `paidInterest` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `overdraftTime` VARCHAR(50) NOT NULL,
  `lastInterestTime` VARCHAR(50) NOT NULL,
  `dueDate` VARCHAR(50) NOT NULL,
  `dailyInterestRate` DECIMAL(6,6) NOT NULL DEFAULT 0.0005,
  `status` TINYINT NOT NULL DEFAULT 0, -- 0: 未还清, 1: 已还清
  PRIMARY KEY (`recordId`),
  KEY `idx_overdraft_records_wallet` (`walletId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
