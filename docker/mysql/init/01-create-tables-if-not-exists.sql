CREATE DATABASE IF NOT EXISTS `elm`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

CREATE DATABASE IF NOT EXISTS `elm2`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE `elm2`;

CREATE TABLE IF NOT EXISTS `user` (
  `userId` VARCHAR(50) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `userName` VARCHAR(100) NOT NULL,
  `userSex` TINYINT DEFAULT NULL,
  `userImg` VARCHAR(255) DEFAULT NULL,
  `delTag` TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`userId`),
  KEY `idx_user_userName` (`userName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `business` (
  `businessId` INT NOT NULL AUTO_INCREMENT,
  `businessName` VARCHAR(100) NOT NULL,
  `businessAddress` VARCHAR(255) DEFAULT NULL,
  `businessExplain` VARCHAR(255) DEFAULT NULL,
  `businessImg` VARCHAR(255) DEFAULT NULL,
  `orderTypeId` INT DEFAULT NULL,
  `starPrice` DECIMAL(10,2) DEFAULT 0.00,
  `deliveryPrice` DECIMAL(10,2) DEFAULT 0.00,
  `remarks` VARCHAR(255) DEFAULT NULL,
  `userId` VARCHAR(50) DEFAULT NULL,
  PRIMARY KEY (`businessId`),
  KEY `idx_business_orderTypeId` (`orderTypeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `food` (
  `foodId` INT NOT NULL AUTO_INCREMENT,
  `foodName` VARCHAR(100) NOT NULL,
  `foodExplain` VARCHAR(255) DEFAULT NULL,
  `foodImg` VARCHAR(255) DEFAULT NULL,
  `foodPrice` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `businessId` INT NOT NULL,
  `remarks` VARCHAR(255) DEFAULT NULL,
  `valid` TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`foodId`),
  KEY `idx_food_businessId` (`businessId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `cart` (
  `cartId` INT NOT NULL AUTO_INCREMENT,
  `foodId` INT NOT NULL,
  `businessId` INT NOT NULL,
  `userId` VARCHAR(50) NOT NULL,
  `quantity` INT NOT NULL DEFAULT 1,
  PRIMARY KEY (`cartId`),
  UNIQUE KEY `uk_cart_food_business_user` (`foodId`, `businessId`, `userId`),
  KEY `idx_cart_user_business` (`userId`, `businessId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `deliveryAddress` (
  `daId` INT NOT NULL AUTO_INCREMENT,
  `contactName` VARCHAR(50) NOT NULL,
  `contactSex` TINYINT DEFAULT NULL,
  `contactTel` VARCHAR(30) DEFAULT NULL,
  `address` VARCHAR(255) NOT NULL,
  `userId` VARCHAR(50) NOT NULL,
  `valid` TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`daId`),
  KEY `idx_deliveryAddress_user_valid` (`userId`, `valid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `orders` (
  `orderId` INT NOT NULL AUTO_INCREMENT,
  `userId` VARCHAR(50) NOT NULL,
  `businessId` INT NOT NULL,
  `orderDate` VARCHAR(50) DEFAULT NULL,
  `orderTotal` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `daId` INT DEFAULT NULL,
  `orderState` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`orderId`),
  KEY `idx_orders_userId` (`userId`),
  KEY `idx_orders_businessId` (`businessId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `orderDetailet` (
  `odId` INT NOT NULL AUTO_INCREMENT,
  `orderId` INT NOT NULL,
  `foodId` INT NOT NULL,
  `quantity` INT NOT NULL,
  PRIMARY KEY (`odId`),
  KEY `idx_orderDetailet_orderId` (`orderId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `VirtualWallet` (
  `walletId` INT NOT NULL AUTO_INCREMENT,
  `createTime` VARCHAR(50) DEFAULT NULL,
  `userId` VARCHAR(50) NOT NULL,
  `balance` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  PRIMARY KEY (`walletId`),
  UNIQUE KEY `uk_virtual_wallet_user` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `VirtualWalletDetails` (
  `detailsId` INT NOT NULL AUTO_INCREMENT,
  `time` VARCHAR(50) DEFAULT NULL,
  `amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `type` INT NOT NULL,
  `fromWalletId` INT DEFAULT NULL,
  `toWalletId` INT DEFAULT NULL,
  PRIMARY KEY (`detailsId`),
  KEY `idx_wallet_details_from` (`fromWalletId`),
  KEY `idx_wallet_details_to` (`toWalletId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `integral` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `amount` INT NOT NULL,
  `type` VARCHAR(20) NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `channel` VARCHAR(50) NOT NULL,
  `expire_time` DATETIME NOT NULL,
  `business_id` BIGINT DEFAULT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `remaining_amount` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_integral_user_id` (`user_id`),
  KEY `idx_integral_status` (`status`),
  KEY `idx_integral_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sign_in_records` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `sign_date` DATE NOT NULL,
  `consecutive_days` INT NOT NULL DEFAULT 1,
  `points_awarded` INT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_sign_in_user_date` (`user_id`, `sign_date`),
  KEY `idx_sign_in_user_id` (`user_id`),
  KEY `idx_sign_in_sign_date` (`sign_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `birthday_integral_records` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `record_date` DATE NOT NULL,
  `monthly_earned` BOOLEAN NOT NULL DEFAULT FALSE,
  `birthday_earned` BOOLEAN NOT NULL DEFAULT FALSE,
  `monthly_points` INT DEFAULT 0,
  `birthday_points` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_birthday_user_date` (`user_id`, `record_date`),
  KEY `idx_birthday_user_id` (`user_id`),
  KEY `idx_birthday_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
