CREATE TABLE if not exists `ipAddr` (
	`idrec` MEDIUMINT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
	`stamp` BIGINT(13) UNSIGNED NOT NULL DEFAULT '442278000000',
	`squidans` VARCHAR(20) NOT NULL DEFAULT 'unknown',
	`bytes` INT(11) NOT NULL DEFAULT '42',
	`timespend` INT(11) NOT NULL DEFAULT '42',
	`site` VARCHAR(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
	PRIMARY KEY (`idrec`),
	UNIQUE INDEX `stamp` (`stamp`, `site`, `bytes`) USING BTREE,
	INDEX `site` (`site`)
)
COMMENT='pcName'
COLLATE='utf8_general_ci'
ENGINE=MyISAM
CHECKSUM=1
;