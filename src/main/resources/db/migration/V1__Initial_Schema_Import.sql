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
    UNIQUE KEY `title_unique` (`title`),
    INDEX `owner_idx` (`owner`)
);

CREATE TABLE `group_lists` (
    `id` INTEGER(20) NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `content_type` VARCHAR(64) NOT NULL,
    `privacy_type` VARCHAR(64) NOT NULL,
    `owner` VARCHAR(255) NOT NULL,
    PRIMARY KEY `primary` (`id`),
    UNIQUE KEY `name_unique` (`name`),
    INDEX `owner_idx` (`owner`)
);

CREATE TABLE `group_contents` (
    `id` INTEGER(20) NOT NULL AUTO_INCREMENT,
    `group_id` INTEGER(20) NOT NULL,
    `content_id` INTEGER(20) NOT NULL,
    PRIMARY KEY `primary` (`id`),
    FOREIGN KEY `group_fk` (`group_id`) REFERENCES `group_lists` (`id`) ON DELETE CASCADE
);

CREATE TABLE `locations` (
    `id` INTEGER(20) NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `privacy_type` VARCHAR(64) NOT NULL,
    `owner` VARCHAR(255) NOT NULL,
    PRIMARY KEY `primary` (`id`),
    UNIQUE KEY `name_unique` (`name`),
    INDEX `owner_idx` (`owner`)
);

CREATE TABLE `location_links` (
    `id` INTEGER(20) NOT NULL AUTO_INCREMENT,
    `dest` INTEGER(20) NOT NULL,
    `origin` INTEGER(20) NOT NULL,
    `short_desc` VARCHAR(255) NOT NULL,
    `privacy_type` VARCHAR(64) NOT NULL,
    PRIMARY KEY `primary` (`id`),
    UNIQUE KEY `nodupes_origin_dest` (`origin`, `dest`),
    FOREIGN KEY `origin_fk` (`origin`) REFERENCES `locations` (`id`) ON DELETE CASCADE,
    FOREIGN KEY `dest_fk` (`dest`) REFERENCES `locations` (`id`) ON DELETE CASCADE
);


