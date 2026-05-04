USE `elm2`;

ALTER TABLE `integral`
    MODIFY COLUMN `user_id` VARCHAR(50) NOT NULL COMMENT '用户ID';

ALTER TABLE `sign_in_records`
    MODIFY COLUMN `user_id` VARCHAR(50) NOT NULL COMMENT '用户ID';

ALTER TABLE `birthday_integral_records`
    MODIFY COLUMN `user_id` VARCHAR(50) NOT NULL COMMENT '用户ID';
