create TABLE `ipAddr` (
	`idrec` MEDIUMINT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
	`stamp` BIGINT(13) UNSIGNED NOT NULL DEFAULT '442278000000',
	`squidans` VARCHAR(20) NOT NULL DEFAULT 'unknown',
	`bytes` INT(11) NOT NULL DEFAULT '42',
	`timespend` INT(11) NOT NULL DEFAULT '42',
	`site` VARCHAR(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
	PRIMARY KEY (`idrec`),
	UNIQUE INDEX `stamp` (`stamp`, `site`, `bytes`) USING HASH
)
COMMENT='pcName'
COLLATE='utf8_general_ci'
ENGINE=MyISAM
ROW_FORMAT=COMPRESSED
AUTO_INCREMENT=1
;

/*
{
        return "CREATE TABLE if not exists `ipAddr` (\n" +
                "\t`idrec` MEDIUMINT(11) UNSIGNED NOT NULL AUTO_INCREMENT,\n" +
                "\t`stamp` BIGINT(13) UNSIGNED NOT NULL DEFAULT '442278000000',\n" +
                "\t`squidans` VARCHAR(20) NOT NULL DEFAULT 'unknown',\n" +
                "\t`bytes` INT(11) NOT NULL DEFAULT '42',\n" +
                "\t`timespend` INT(11) NOT NULL DEFAULT '42',\n" +
                "\t`site` VARCHAR(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',\n" +
                "\tPRIMARY KEY (`idrec`),\n" +
                "\tUNIQUE INDEX `stamp` (`stamp`, `site`, `bytes`) USING BTREE,\n" +
                "\tINDEX `site` (`site`)\n" +
                ")\n" +
                "COMMENT='pcName'\n" +
                "COLLATE='utf8_general_ci'\n" +
                "ENGINE=MyISAM\n" +
                "CHECKSUM=1\n" +
                ";".replace(ConstantsFor.FIELDNAME_ADDR, this.ipAddr).replace(ConstantsFor.DBFIELD_PCNAME, PCInfo.checkValidNameWithoutEatmeat(ipAddr));
    }*/
