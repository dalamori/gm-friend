CREATE DATABASE IF NOT EXISTS `dm_friend`;

USE dm_friend;

CREATE TABLE `properties` (
    `id` INTEGER(20) NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `value` VARCHAR(255) NOT NULL,
    `property_type` VARCHAR(64) NOT NULL,
    `privacy_type` VARCHAR(64) NOT NULL,
    `owner` VARCHAR(255) NOT NULL,
    PRIMARY KEY `primary` (`id`),
    INDEX `owner_idx` (`owner`)
);

CREATE TABLE `notes` (
    `id` INTEGER(20) NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(255) NOT NULL,
    `body` LONGTEXT NOT NULL,
    `privacy_type` VARCHAR(64) NOT NULL,
    `owner` VARCHAR(255) NOT NULL,
    PRIMARY KEY `primary` (`id`),
    UNIQUE KEY `title_idx` (`title`),
    INDEX `owner_idx` (`owner`)
);


