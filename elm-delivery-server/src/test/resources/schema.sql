CREATE TABLE IF NOT EXISTS deliveryAddress (
    daId INT AUTO_INCREMENT PRIMARY KEY,
    contactName VARCHAR(50) NOT NULL,
    contactSex TINYINT,
    contactTel VARCHAR(30),
    address VARCHAR(255) NOT NULL,
    userId VARCHAR(50) NOT NULL,
    valid TINYINT NOT NULL DEFAULT 1
);
