

ALTER TABLE `core_user` ADD COLUMN `userPrincipalName` VARCHAR(255) NULL DEFAULT NULL;
ALTER TABLE `core_ldap` ADD COLUMN `dc_rank` INT NULL DEFAULT '0';
ALTER TABLE `core_ldap` ADD COLUMN `mapEmailDomains` VARCHAR(255) NULL DEFAULT NULL;
