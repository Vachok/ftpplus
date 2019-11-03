CREATE DATABASE IF NOT EXISTS `archive`;
USE `archive`;

CREATE TABLE IF NOT EXISTS `velkompc` (
  `idrec` mediumint(7) unsigned NOT NULL AUTO_INCREMENT,
  `NamePP` varchar(20) DEFAULT NULL,
  `AddressPP` tinytext,
  `SegmentPP` varchar(200) DEFAULT NULL,
  `instr` varchar(20) DEFAULT NULL,
  `OnlineNow` tinyint(1) DEFAULT NULL,
  `userName` char(110) NOT NULL DEFAULT 'No user resolved',
  `TimeNow` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`idrec`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 MAX_ROWS=2500000 ROW_FORMAT=COMPRESSED COMMENT='Scan Stats for all time';

CREATE DATABASE IF NOT EXISTS `common`;
USE `common`;

CREATE TABLE IF NOT EXISTS `common` (
  `idrec` int(11) NOT NULL AUTO_INCREMENT,
  `dir` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `user` varchar(255) NOT NULL,
  `users` text NOT NULL,
  `timerec` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `dir` (`dir`),
  KEY `user` (`user`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Права пользователей на папки';

CREATE TABLE IF NOT EXISTS `muchsymbols` (
  `idrec` int(11) NOT NULL AUTO_INCREMENT,
  `tstamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `path` varchar(1000) NOT NULL DEFAULT '',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `path` (`path`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 ROW_FORMAT=FIXED COMMENT='Проблемные пути. (Path > 254 symbols)';

CREATE TABLE IF NOT EXISTS `oldfiles` (
  `idrec` int(7) unsigned NOT NULL AUTO_INCREMENT COMMENT 'pimary id',
  `AbsolutePath` varchar(255) NOT NULL DEFAULT 'error' COMMENT 'Абсолютный путь до файла',
  `size` float NOT NULL DEFAULT '0' COMMENT 'Размер в мегабайтах',
  `Attributes` text NOT NULL COMMENT 'Атрибуты',
  `tstamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Время детекции',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `AbsolutePath` (`AbsolutePath`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='Файлы более 15 мб в которые не заходили более 2х лет';

CREATE TABLE IF NOT EXISTS `restore` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `upstring` varchar(260) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `upstring` (`upstring`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=FIXED COMMENT='Файлы, для восстановлния';

CREATE DATABASE IF NOT EXISTS `inetstats` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `inetstats`;

CREATE TABLE IF NOT EXISTS `10_10_30_30` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='Unknown user: 10.10.30.30\n ResolveUserInDataBase[\naboutWhat = 10.10.30.30,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_10_35_30` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '0',
  `squidans` varchar(20) NOT NULL DEFAULT 'TCP',
  `bytes` int(11) NOT NULL DEFAULT '0',
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL DEFAULT 'velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='Unknown user: 10.10.35.30\n ResolveUserInDataBase[\naboutWhat = 10.10.35.30,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_200_156` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user td-vel-pos-6.eatmeat.ru : ResolveUserInDataBase[\naboutWhat = Unknown PC: td-vel-pos-6.eatmeat.ru\n class ru.vachok.networker.ad.pc.PCInfo,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_200_159` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user td-vel-pos-9.eatmeat.ru : ResolveUserInDataBase[\naboutWhat = Unknown PC: td-vel-pos-9.eatmeat.ru\n class ru.vachok.networker.ad.pc.PCInfo,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_200_55` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0016 : IPavluchenkov : 26.10.19 8:24';

CREATE TABLE IF NOT EXISTS `10_200_200_57` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0018 : kpivovarov : 26.10.19 8:24';

CREATE TABLE IF NOT EXISTS `10_200_200_58` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='no0007 : kpivovarov : 25.10.19 17:05';

CREATE TABLE IF NOT EXISTS `10_200_201_108` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='Unknown user a304.eatmeat.ru : ResolveUserInDataBase[\naboutWhat = a304,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_201_146` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0069 : a.a.osipova : 25.10.19 17:07';

CREATE TABLE IF NOT EXISTS `10_200_202_150` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 10.200.202.150 : ResolveUserInDataBase[\naboutWhat = 10.200.202.150,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_202_52` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='Unknown user: 10.200.202.52\n ResolveUserInDataBase[\naboutWhat = 10.200.202.52,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_202_54` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='Unknown user b235.eatmeat.ru : ResolveUserInDataBase[\naboutWhat = Unknown PC: b235.eatmeat.ru\n class ru.vachok.networker.ad.pc.PCInfo,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_202_55` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING HASH
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0033 : Asemenov : 27.10.19 18:46';

CREATE TABLE IF NOT EXISTS `10_200_202_56` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0101 : s.s.kalinkin : 25.10.19 17:08';

CREATE TABLE IF NOT EXISTS `10_200_202_63` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0017 : lshvab : 25.10.19 14:38';

CREATE TABLE IF NOT EXISTS `10_200_202_66` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0007 : d.vodennikov : 25.10.19 17:24';

CREATE TABLE IF NOT EXISTS `10_200_202_70` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0119 : o.g.hramov : 25.10.19 16:34';

CREATE TABLE IF NOT EXISTS `10_200_202_71` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='Unknown user dp0002.eatmeat.ru : ResolveUserInDataBase[\naboutWhat = dp0002,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_202_75` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '-666',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='Unknown user: 10.200.202.75';

CREATE TABLE IF NOT EXISTS `10_200_202_77` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0221 : a.a.redkin : 25.10.19 17:00';

CREATE TABLE IF NOT EXISTS `10_200_202_84` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='Unknown user: 10.200.202.84';

CREATE TABLE IF NOT EXISTS `10_200_202_86` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='Unknown user: do0035';

CREATE TABLE IF NOT EXISTS `10_200_203_102` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 10.200.203.102 : ResolveUserInDataBase[\naboutWhat = 10.200.203.102,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_203_108` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='td008 : etsaturyan : 27.10.19 19:01';

CREATE TABLE IF NOT EXISTS `10_200_204_136` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Unknown user: 10.200.204.136';

CREATE TABLE IF NOT EXISTS `10_200_204_137` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Unknown user: 10.200.204.137';

CREATE TABLE IF NOT EXISTS `10_200_204_51` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='Unknown user 10.200.204.51 : ResolveUserInDataBase[\naboutWhat = 10.200.204.51,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_204_52` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0059 : OUGPSK : 27.10.19 20:46';

CREATE TABLE IF NOT EXISTS `10_200_204_60` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: pp0016.eatmeat.ru\n ResolveUserInDataBase[\naboutWhat = pp0016,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_204_64` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Unknown user: 10.200.204.64';

CREATE TABLE IF NOT EXISTS `10_200_204_71` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Unknown user: 10.200.204.71';

CREATE TABLE IF NOT EXISTS `10_200_204_86` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Unknown user: 10.200.204.86';

CREATE TABLE IF NOT EXISTS `10_200_205_52` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0048 : oppf : 27.10.19 20:45';

CREATE TABLE IF NOT EXISTS `10_200_207_87` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Unknown user: 10.200.207.87';

CREATE TABLE IF NOT EXISTS `10_200_208_52` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0083 : pvinnichenko : 27.10.19 19:18';

CREATE TABLE IF NOT EXISTS `10_200_208_53` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='a228 : gvvopps : 27.10.19 18:32';

CREATE TABLE IF NOT EXISTS `10_200_208_55` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='a184 : a.v.komarov : 27.10.19 18:30';

CREATE TABLE IF NOT EXISTS `10_200_208_60` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0077 : Azdorovetskaya : 27.10.19 19:17';

CREATE TABLE IF NOT EXISTS `10_200_208_63` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0065 : vrevyakin : 27.10.19 19:16';

CREATE TABLE IF NOT EXISTS `10_200_208_65` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='Unknown user: pp0007';

CREATE TABLE IF NOT EXISTS `10_200_210_51` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0022 : mkc : 27.10.19 19:28';

CREATE TABLE IF NOT EXISTS `10_200_210_55` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Unknown user: 10.200.210.55';

CREATE TABLE IF NOT EXISTS `10_200_210_58` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Unknown user: 10.200.210.58';

CREATE TABLE IF NOT EXISTS `10_200_210_64` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Unknown user: 10.200.210.64';

CREATE TABLE IF NOT EXISTS `10_200_212_52` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='a186 : kpivovarov : 27.10.19 18:30';

CREATE TABLE IF NOT EXISTS `10_200_212_54` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Unknown user: 10.200.212.54';

CREATE TABLE IF NOT EXISTS `10_200_212_56` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 MAX_ROWS=200000 CHECKSUM=1 ROW_FORMAT=COMPRESSED COMMENT='a223 : OPKS : 27.10.19 18:32';

CREATE TABLE IF NOT EXISTS `10_200_212_57` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='pp0041.eatmeat.ru';

CREATE TABLE IF NOT EXISTS `10_200_212_62` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Unknown user a153.eatmeat.ru : ResolveUserInDataBase[\naboutWhat = a153,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_212_65` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0082 : d.yu.podbuckii : 27.10.19 19:18';

CREATE TABLE IF NOT EXISTS `10_200_212_66` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Unknown user: 10.200.212.66';

CREATE TABLE IF NOT EXISTS `10_200_212_86` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0217 : d.yu.podbuckii : 26.10.19 7:28';

CREATE TABLE IF NOT EXISTS `10_200_213_100` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='dotd0005 : o.n.chizhova : 27.10.19 18:38';

CREATE TABLE IF NOT EXISTS `10_200_213_103` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0001 : estrelyaeva';

CREATE TABLE IF NOT EXISTS `10_200_213_105` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0114 : ssavenkova : 25.10.19 17:09';

CREATE TABLE IF NOT EXISTS `10_200_213_110` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0012 : smironova : 25.10.19 17:02';

CREATE TABLE IF NOT EXISTS `10_200_213_111` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0014 : odragan : 25.10.19 17:02';

CREATE TABLE IF NOT EXISTS `10_200_213_112` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0105 : kazna : 25.10.19 16:33';

CREATE TABLE IF NOT EXISTS `10_200_213_113` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='a282 : t.m.letuhina : 25.10.19 16:48';

CREATE TABLE IF NOT EXISTS `10_200_213_114` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='a306 : evatrusheva : 27.10.19 20:31';

CREATE TABLE IF NOT EXISTS `10_200_213_116` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='Unknown user: td009';

CREATE TABLE IF NOT EXISTS `10_200_213_117` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='td006 : o.n.shamanskaya : 27.10.19 19:00';

CREATE TABLE IF NOT EXISTS `10_200_213_119` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0005 : vermakova : 27.10.19 1:34';

CREATE TABLE IF NOT EXISTS `10_200_213_120` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0019 : GTarasova : 27.10.19 16:29';

CREATE TABLE IF NOT EXISTS `10_200_213_121` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0002 : kpivovarov : 25.10.19 17:24';

CREATE TABLE IF NOT EXISTS `10_200_213_124` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0068 : efilistova : 27.10.19 20:47';

CREATE TABLE IF NOT EXISTS `10_200_213_125` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0047 : gchernova : 25.10.19 17:27';

CREATE TABLE IF NOT EXISTS `10_200_213_127` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0055 : l.baybakova : 27.10.19 18:47';

CREATE TABLE IF NOT EXISTS `10_200_213_128` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0003 : strofimova : 27.10.19 19:27';

CREATE TABLE IF NOT EXISTS `10_200_213_131` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='a301 : gguscha : 25.10.19 17:20';

CREATE TABLE IF NOT EXISTS `10_200_213_132` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 ROW_FORMAT=COMPRESSED COMMENT='do0009 : BUH_SCH3 : 27.10.19 20:44';

CREATE TABLE IF NOT EXISTS `10_200_213_133` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0030 : i.v.klyuchevaya : 25.10.19 15:10';

CREATE TABLE IF NOT EXISTS `10_200_213_134` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0053 : planirovanie : 27.10.19 18:47';

CREATE TABLE IF NOT EXISTS `10_200_213_136` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0131 : e.d.podkovyrov';

CREATE TABLE IF NOT EXISTS `10_200_213_137` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='a293 : kpivovarov : 27.10.19 20:31';

CREATE TABLE IF NOT EXISTS `10_200_213_139` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0050 : e.s.trofimenko : 25.10.19 17:27';

CREATE TABLE IF NOT EXISTS `10_200_213_143` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0206 : a.i.mullenkova : 27.10.19 18:51';

CREATE TABLE IF NOT EXISTS `10_200_213_144` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='a309 : v.yu.zaharova : 27.10.19 20:32';

CREATE TABLE IF NOT EXISTS `10_200_213_145` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0102 : a.filistov : 25.10.19 17:30';

CREATE TABLE IF NOT EXISTS `10_200_213_146` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0051 : aarhipichev : 25.10.19 17:27';

CREATE TABLE IF NOT EXISTS `10_200_213_147` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0071 : n.krutova : 27.10.19 19:32';

CREATE TABLE IF NOT EXISTS `10_200_213_148` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0052 : o.n.mutnykh : 25.10.19 17:05';

CREATE TABLE IF NOT EXISTS `10_200_213_149` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='a188 : a.a.pravdyukova : 25.10.19 17:14';

CREATE TABLE IF NOT EXISTS `10_200_213_163` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='no0035 : kpivovarov : 25.10.19 17:38';

CREATE TABLE IF NOT EXISTS `10_200_213_166` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='a303 : e.d.podkovyrov : 27.10.19 18:36';

CREATE TABLE IF NOT EXISTS `10_200_213_167` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0043 : a.chernyshova : 23.10.19 15:45';

CREATE TABLE IF NOT EXISTS `10_200_213_173` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0099 : e.d.podkovyrov : 22.10.19 11:53';

CREATE TABLE IF NOT EXISTS `10_200_213_189` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0110 : a.m.gladkova : 25.10.19 17:30';

CREATE TABLE IF NOT EXISTS `10_200_213_190` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING HASH
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0004 : e.vyrodova';

CREATE TABLE IF NOT EXISTS `10_200_213_200` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING HASH
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0045 : kpivovarov';

CREATE TABLE IF NOT EXISTS `10_200_213_55` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0205 : pyanushkin : 25.10.19 14:44';

CREATE TABLE IF NOT EXISTS `10_200_213_56` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0091 : vkuharenko : 25.10.19 17:30';

CREATE TABLE IF NOT EXISTS `10_200_213_59` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='a118 : yu.m.pikuleva : 25.10.19 17:11';

CREATE TABLE IF NOT EXISTS `10_200_213_65` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0128 : v.a.vorobev : 27.10.19 18:50';

CREATE TABLE IF NOT EXISTS `10_200_213_66` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`site`,`stamp`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0106 : mponkratov : 27.10.19 15:59';

CREATE TABLE IF NOT EXISTS `10_200_213_72` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0020 : a.s.davidov : 27.10.19 19:28';

CREATE TABLE IF NOT EXISTS `10_200_213_74` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0029 : k.korshunova : 25.10.19 17:26';

CREATE TABLE IF NOT EXISTS `10_200_213_75` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0040 : efetisova : 25.10.19 17:26';

CREATE TABLE IF NOT EXISTS `10_200_213_78` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING HASH
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0046 : m.a.hudobina : 25.10.19 17:27';

CREATE TABLE IF NOT EXISTS `10_200_213_81` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING HASH
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0116 : k.a.shubin : 25.10.19 17:31';

CREATE TABLE IF NOT EXISTS `10_200_213_85` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING HASH
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user Unknown PC: test.test.eatmeat.ru\n class ru.vachok.networker.ad.pc.PCInfo : ResolveUserInDataBase[\naboutWhat = Unknown PC: Unknown PC: test.test.eatmeat.ru\n class ru.vachok.networker.ad.pc.PCInfo\n class ru.vachok.networker.ad.pc.PCInfo,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_213_92` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING HASH
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0058 : DPETROV : 22.10.19 10:23';

CREATE TABLE IF NOT EXISTS `10_200_213_94` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING HASH
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0013 : s.zhilina : 23.10.19 17:26';

CREATE TABLE IF NOT EXISTS `10_200_213_97` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='dotd0007 : v.plahina : 27.10.19 18:38';

CREATE TABLE IF NOT EXISTS `10_200_213_98` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='a115 : p.g.kovalchuk : 23.10.19 15:45';

CREATE TABLE IF NOT EXISTS `10_200_214_100` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 10.200.214.100 : ResolveUserInDataBase[\naboutWhat = 10.200.214.100,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_214_102` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='no0029 : kpivovarov : 27.10.19 19:04';

CREATE TABLE IF NOT EXISTS `10_200_214_105` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 10.200.214.105\n';

CREATE TABLE IF NOT EXISTS `10_200_214_112` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 10.200.214.112\n';

CREATE TABLE IF NOT EXISTS `10_200_214_116` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 10.200.214.116';

CREATE TABLE IF NOT EXISTS `10_200_214_117` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 10.200.214.117\n ResolveUserInDataBase[\naboutWhat = 10.200.214.117,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_214_120` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 10.200.214.120';

CREATE TABLE IF NOT EXISTS `10_200_214_121` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='a244 : i.n.kuznecov : 22.10.19 17:15';

CREATE TABLE IF NOT EXISTS `10_200_214_122` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='no0039 : a.m.gladkova : 25.10.19 11:26';

CREATE TABLE IF NOT EXISTS `10_200_214_124` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 10.200.214.124';

CREATE TABLE IF NOT EXISTS `10_200_214_128` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Unknown user: 10.200.214.128';

CREATE TABLE IF NOT EXISTS `10_200_214_132` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 10.200.214.132';

CREATE TABLE IF NOT EXISTS `10_200_214_51` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: a265.eatmeat.ru';

CREATE TABLE IF NOT EXISTS `10_200_214_53` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING HASH
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0001 : estrelyaeva : 25.10.19 17:24';

CREATE TABLE IF NOT EXISTS `10_200_214_57` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 10.200.214.57';

CREATE TABLE IF NOT EXISTS `10_200_214_60` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='a250 : o.n.shpenkler : 27.10.19 18:34';

CREATE TABLE IF NOT EXISTS `10_200_214_70` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 10.200.214.70 : ResolveUserInDataBase[\naboutWhat = 10.200.214.70,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_214_73` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 10.200.214.73';

CREATE TABLE IF NOT EXISTS `10_200_214_74` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='no0010 : o.v.kuraeva : 27.10.19 17:50';

CREATE TABLE IF NOT EXISTS `10_200_214_78` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='no0027 : kpivovarov : 22.10.19 17:06';

CREATE TABLE IF NOT EXISTS `10_200_214_81` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 10.200.214.81\n ResolveUserInDataBase[\naboutWhat = 10.200.214.81,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_214_85` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 10.200.214.85 : ResolveUserInDataBase[\naboutWhat = 10.200.214.85,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_214_91` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: no0032.eatmeat.ru\n ResolveUserInDataBase[\naboutWhat = no0032,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_217_102` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0133 : buh_sch2 : 27.10.19 19:35';

CREATE TABLE IF NOT EXISTS `10_200_217_104` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0041 : g.gavrileeva : 25.10.19 17:26';

CREATE TABLE IF NOT EXISTS `10_200_217_51` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='a224 : ialekseeva : 27.10.19 18:32';

CREATE TABLE IF NOT EXISTS `10_200_217_52` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0039 : lnageykina : 25.10.19 17:26';

CREATE TABLE IF NOT EXISTS `10_200_217_54` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0038 : okrasilnikova : 25.10.19 17:26';

CREATE TABLE IF NOT EXISTS `10_200_217_55` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0028 : a.raitmanova : 24.10.19 12:11';

CREATE TABLE IF NOT EXISTS `10_200_217_56` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0214 : s.m.pavlova : 27.10.19 20:51';

CREATE TABLE IF NOT EXISTS `10_200_217_58` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0111 : o.loginovskaya : 27.10.19 20:49';

CREATE TABLE IF NOT EXISTS `10_200_217_60` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0061 : n.orlova : 25.10.19 17:28';

CREATE TABLE IF NOT EXISTS `10_200_217_61` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0025 : kshilo : 27.10.19 19:29';

CREATE TABLE IF NOT EXISTS `10_200_217_62` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0024 : s.krupelnitskiy : 27.10.19 19:29';

CREATE TABLE IF NOT EXISTS `10_200_217_63` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='a291 : s.krupelnitskiy : 27.10.19 20:31';

CREATE TABLE IF NOT EXISTS `10_200_217_64` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0126 : v.yu.ivanova : 27.10.19 19:35';

CREATE TABLE IF NOT EXISTS `10_200_217_66` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0027 : a.a.simonova : 27.10.19 19:29';

CREATE TABLE IF NOT EXISTS `10_200_217_67` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0073 : a.kotelnikov : 27.10.19 19:32';

CREATE TABLE IF NOT EXISTS `10_200_217_68` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0015 : o.yu.rosti : 27.10.19 19:28';

CREATE TABLE IF NOT EXISTS `10_200_217_69` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0032 : coord_spb : 27.10.19 19:29';

CREATE TABLE IF NOT EXISTS `10_200_217_70` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='a123 : a.f.zarickii : 27.10.19 18:27';

CREATE TABLE IF NOT EXISTS `10_200_217_72` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0026 : m.Khusyainov : 22.10.19 17:56';

CREATE TABLE IF NOT EXISTS `10_200_217_75` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0054 : Eruzieva : 25.10.19 17:27';

CREATE TABLE IF NOT EXISTS `10_200_217_76` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0037 : ovarvaricheva.EATMEAT : 27.10.19 19:29';

CREATE TABLE IF NOT EXISTS `10_200_217_77` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0011 : pvinnichenko : 25.10.19 17:25';

CREATE TABLE IF NOT EXISTS `10_200_217_78` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0138 : d.a.voskresenskii : 27.10.19 19:36';

CREATE TABLE IF NOT EXISTS `10_200_217_79` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0044 : e.v.vinokur : 27.10.19 20:45';

CREATE TABLE IF NOT EXISTS `10_200_217_80` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0070 : dezopov : 25.10.19 9:33';

CREATE TABLE IF NOT EXISTS `10_200_217_81` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0031 : okrasnova : 27.10.19 19:29';

CREATE TABLE IF NOT EXISTS `10_200_217_83` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0008 : v.yu.zaharova : 27.10.19 19:28';

CREATE TABLE IF NOT EXISTS `10_200_217_86` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0036 : i.n.kuznecov : 27.10.19 15:56';

CREATE TABLE IF NOT EXISTS `10_200_217_89` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0120 : a.v.mironenko : 27.10.19 19:35';

CREATE TABLE IF NOT EXISTS `10_200_217_90` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0127 : e.s.sazonkina : 25.10.19 15:50';

CREATE TABLE IF NOT EXISTS `10_200_217_93` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0112 : buh_sch4 : 27.10.19 19:34';

CREATE TABLE IF NOT EXISTS `10_200_217_94` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0078 : e.r.izmajlova : 27.10.19 19:32';

CREATE TABLE IF NOT EXISTS `10_200_218_53` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 10.200.218.53 : ResolveUserInDataBase[\naboutWhat = 10.200.218.53,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_218_54` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0207 : v.v.andrianova : 25.10.19 17:00';

CREATE TABLE IF NOT EXISTS `10_200_218_56` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0209 : n.n.poloshevec : 25.10.19 17:00';

CREATE TABLE IF NOT EXISTS `10_200_218_59` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: a201.eatmeat.ru\n ResolveUserInDataBase[\naboutWhat = a201,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_218_60` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0203 : i.s.yumashina : 25.10.19 19:09';

CREATE TABLE IF NOT EXISTS `10_200_218_61` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 10.200.218.61\n ResolveUserInDataBase[\naboutWhat = 10.200.218.61,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_218_64` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 10.200.218.64\n ResolveUserInDataBase[\naboutWhat = 10.200.218.64,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `10_200_218_66` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0062 : n.m.selivanov : 25.10.19 17:28';

CREATE TABLE IF NOT EXISTS `10_200_218_67` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0212 : d.yu.podbuckii : 27.10.19 19:37';

CREATE TABLE IF NOT EXISTS `10_200_218_68` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0074 : s.v.travina : 25.10.19 17:29';

CREATE TABLE IF NOT EXISTS `172_16_200_6` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 172.16.200.6 : ResolveUserInDataBase[\naboutWhat = 172.16.200.6,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_106` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 192.168.13.106 : ResolveUserInDataBase[\naboutWhat = 192.168.13.106,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_109` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.109\n ResolveUserInDataBase[\naboutWhat = 192.168.13.109,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_113` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Unknown user: srv-ra.eatmeat.ru\n ResolveUserInDataBase[\naboutWhat = Unknown PC: srv-ra.eatmeat.ru\n class ru.vachok.networker.ad.pc.PCInfo,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_118` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='a323 : tbabicheva : 26.10.19 0:31';

CREATE TABLE IF NOT EXISTS `192_168_13_125` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 192.168.13.125 : ResolveUserInDataBase[\naboutWhat = 192.168.13.125,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_130` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: a165.eatmeat.ru\n ResolveUserInDataBase[\naboutWhat = a165,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_134` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 192.168.13.134 : ResolveUserInDataBase[\naboutWhat = 192.168.13.134,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_138` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 192.168.13.138 : ResolveUserInDataBase[\naboutWhat = 192.168.13.138,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_143` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0218 : d.yu.podbuckii : 25.10.19 16:37';

CREATE TABLE IF NOT EXISTS `192_168_13_148` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0097 : ashkolkin : 25.10.19 11:08';

CREATE TABLE IF NOT EXISTS `192_168_13_152` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='do0010 : etsatsura : 25.10.19 17:25';

CREATE TABLE IF NOT EXISTS `192_168_13_158` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.158\n ResolveUserInDataBase[\naboutWhat = 192.168.13.158,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_163` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.163\n ResolveUserInDataBase[\naboutWhat = 192.168.13.163,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_170` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Unknown user rupsdpo01.eatmeat.ru : ResolveUserInDataBase[\naboutWhat = Unknown PC: rupsdpo01.eatmeat.ru\n class ru.vachok.networker.ad.pc.PCInfo,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_173` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 192.168.13.173 : ResolveUserInDataBase[\naboutWhat = 192.168.13.173,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_181` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.181\n ResolveUserInDataBase[\naboutWhat = 192.168.13.181,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_182` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.182\n ResolveUserInDataBase[\naboutWhat = 192.168.13.182,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_188` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: iloczj921081f.eatmeat.ru\n ResolveUserInDataBase[\naboutWhat = Unknown PC: iloczj921081f.eatmeat.ru\n class ru.vachok.networker.ad.pc.PCInfo,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_192` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: no0002.eatmeat.ru\n ResolveUserInDataBase[\naboutWhat = no0002,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_194` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.194\n ResolveUserInDataBase[\naboutWhat = 192.168.13.194,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_195` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 192.168.13.195 : ResolveUserInDataBase[\naboutWhat = 192.168.13.195,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_201` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: srv-bi.eatmeat.ru\n ResolveUserInDataBase[\naboutWhat = Unknown PC: srv-bi.eatmeat.ru\n class ru.vachok.networker.ad.pc.PCInfo,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_203` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.203\n ResolveUserInDataBase[\naboutWhat = 192.168.13.203,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_204` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.204\n ResolveUserInDataBase[\naboutWhat = 192.168.13.204,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_206` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.206\n ResolveUserInDataBase[\naboutWhat = 192.168.13.206,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_209` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0140 : a.savchenko : 27.10.19 19:36';

CREATE TABLE IF NOT EXISTS `192_168_13_210` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.210\n ResolveUserInDataBase[\naboutWhat = 192.168.13.210,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_211` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.211\n ResolveUserInDataBase[\naboutWhat = 192.168.13.211,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_217` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: do0109.eatmeat.ru\n ResolveUserInDataBase[\naboutWhat = do0109,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_218` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.218\n ResolveUserInDataBase[\naboutWhat = 192.168.13.218,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_220` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='do0056 : nbelousova : 27.10.19 20:46';

CREATE TABLE IF NOT EXISTS `192_168_13_221` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: tenzo18.eatmeat.ru\n ResolveUserInDataBase[\naboutWhat = Unknown PC: tenzo18.eatmeat.ru\n class ru.vachok.networker.ad.pc.PCInfo,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_223` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: do0021.eatmeat.ru\n ResolveUserInDataBase[\naboutWhat = do0021,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_225` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.225\n ResolveUserInDataBase[\naboutWhat = 192.168.13.225,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_228` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.228\n ResolveUserInDataBase[\naboutWhat = 192.168.13.228,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_230` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 192.168.13.230 : ResolveUserInDataBase[\naboutWhat = 192.168.13.230,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_231` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.231\n ResolveUserInDataBase[\naboutWhat = 192.168.13.231,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_232` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 192.168.13.232 : ResolveUserInDataBase[\naboutWhat = 192.168.13.232,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_235` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.235\n ResolveUserInDataBase[\naboutWhat = 192.168.13.235,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_236` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: a179.eatmeat.ru\n ResolveUserInDataBase[\naboutWhat = a179,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_237` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.237\n ResolveUserInDataBase[\naboutWhat = 192.168.13.237,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_238` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: w10.eatmeat.ru\n ResolveUserInDataBase[\naboutWhat = Unknown PC: w10.eatmeat.ru\n class ru.vachok.networker.ad.pc.PCInfo,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_240` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.240\n ResolveUserInDataBase[\naboutWhat = 192.168.13.240,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_243` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='no0019 : kpivovarov : 27.10.19 19:43';

CREATE TABLE IF NOT EXISTS `192_168_13_244` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.244\n ResolveUserInDataBase[\naboutWhat = 192.168.13.244,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_246` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 192.168.13.246 : ResolveUserInDataBase[\naboutWhat = 192.168.13.246,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_54` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.54\n ResolveUserInDataBase[\naboutWhat = 192.168.13.54,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_57` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: 192.168.13.57\n ResolveUserInDataBase[\naboutWhat = 192.168.13.57,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_69` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0098 : ekaraka : 27.10.19 20:48';

CREATE TABLE IF NOT EXISTS `192_168_13_71` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 192.168.13.71 : ResolveUserInDataBase[\naboutWhat = 192.168.13.71,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_13_92` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Unknown user: 192.168.13.92\n ResolveUserInDataBase[\naboutWhat = 192.168.13.92,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_14_155` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 192.168.14.155 : ResolveUserInDataBase[\naboutWhat = 192.168.14.155,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_14_157` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='do0057 : v.komissarov : 27.10.19 19:31';

CREATE TABLE IF NOT EXISTS `192_168_14_182` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 192.168.14.182 : ResolveUserInDataBase[\naboutWhat = 192.168.14.182,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_14_185` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 192.168.14.185 : ResolveUserInDataBase[\naboutWhat = 192.168.14.185,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_14_191` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 192.168.14.191 : ResolveUserInDataBase[\naboutWhat = 192.168.14.191,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_14_194` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='Unknown user 192.168.14.194 : ResolveUserInDataBase[\naboutWhat = 192.168.14.194,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_14_195` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user 192.168.14.195 : ResolveUserInDataBase[\naboutWhat = 192.168.14.195,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_14_73` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '442278000000',
  `squidans` varchar(20) NOT NULL DEFAULT 'unknown',
  `bytes` int(11) NOT NULL DEFAULT '42',
  `timespend` int(11) NOT NULL DEFAULT '42',
  `site` varchar(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stampkey` (`stamp`,`site`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='Unknown user: pp0040.eatmeat.ru\n ResolveUserInDataBase[\naboutWhat = pp0040,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `192_168_14_84` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '0',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 COMMENT='Unknown user a302.eatmeat.ru : ResolveUserInDataBase[\naboutWhat = a302,\ndataConnectTo = MySqlLocalSRVInetStat{tableName=';

CREATE TABLE IF NOT EXISTS `inetstats` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `Date` bigint(13) unsigned NOT NULL COMMENT 'timestamp',
  `inte` int(11) unsigned NOT NULL COMMENT 'server ans in ms',
  `ip` varchar(20) NOT NULL COMMENT 'ip',
  `response` enum('TAG_NONE/400','TAG_NONE/503','TAG_NONE/505','TAG_NONE_ABORTED/000','TAG_NONE_TIMEDOUT/413','TCP_DENIED/403','TCP_DENIED_ABORTED/403','TCP_DENIED_REPLY/403','TCP_HIT/200','TCP_HIT/206','TCP_INM_HIT/304','TCP_MEM_HIT/200','TCP_MEM_HIT/206','TCP_MISS/200','TCP_MISS/204','TCP_MISS/206','TCP_MISS/301','TCP_MISS/302','TCP_MISS/304','TCP_MISS/307','TCP_MISS/400','TCP_MISS/401','TCP_MISS/403','TCP_MISS/404','TCP_MISS/405','TCP_MISS/502','TCP_MISS/503','TCP_MISS_ABORTED/000','TCP_MISS_ABORTED/200','TCP_MISS_ABORTED/206','TCP_MISS_ABORTED/403','TCP_MISS_TIMEDOUT/200','TCP_REFRESH_MODIFIED/200','TCP_REFRESH_UNMODIFIED/200','TCP_REFRESH_UNMODIFIED/304','TCP_TUNNEL/200') NOT NULL COMMENT 'squid',
  `bytes` int(11) NOT NULL,
  `method` enum('CONNECT','GET','HEAD','NONE','OPTIONS','POST','PUT') NOT NULL COMMENT 'http',
  `site` varchar(254) NOT NULL COMMENT 'url',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `Date` (`Date`,`ip`,`bytes`) USING BTREE,
  KEY `ip` (`ip`)
) ENGINE=MEMORY DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='Текущая интернет статистика';

CREATE TABLE IF NOT EXISTS `test` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `upstring` varchar(260) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `upstring` (`upstring`),
  UNIQUE KEY `upstring_2` (`upstring`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 MAX_ROWS=100000 COMMENT='ALTER';

CREATE DATABASE IF NOT EXISTS `log` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `log`;

DELIMITER //
CREATE DEFINER=`kudr`@`%` EVENT `del log` ON SCHEDULE EVERY 9 HOUR STARTS '2019-10-16 08:30:26' ON COMPLETION PRESERVE ENABLE DO BEGIN
TRUNCATE TABLE log.networker;
INSERT INTO log.networker (classname, msgtype, msgvalue, pc) VALUES ('TRUNCED', CURRENT_TIME(), @id, 'sql');
END//
DELIMITER ;

CREATE TABLE IF NOT EXISTS `networker` (
  `idrec` int(4) NOT NULL AUTO_INCREMENT,
  `classname` varchar(209) NOT NULL DEFAULT 'not set',
  `msgtype` varchar(509) NOT NULL DEFAULT 'not set',
  `msgvalue` varchar(1999) NOT NULL DEFAULT 'not set',
  `pc` varchar(35) NOT NULL DEFAULT 'not set',
  `stack` varchar(9000) NOT NULL DEFAULT '0',
  `stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `upstring` varchar(9999) NOT NULL DEFAULT 'not set',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `classname_pc_stamp` (`classname`,`pc`,`stamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 MAX_ROWS=50000;

CREATE DATABASE IF NOT EXISTS `mem` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `mem`;

CREATE TABLE IF NOT EXISTS `properties` (
  `idrec` int(2) NOT NULL AUTO_INCREMENT COMMENT 'id записи',
  `property` varchar(50) NOT NULL DEFAULT 'Set property here',
  `valueofproperty` varchar(254) NOT NULL DEFAULT 'Set value here',
  `timeSet` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `setter` varchar(50) NOT NULL DEFAULT 'ConstantsFor',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `property_valueofproperty` (`property`,`setter`)
) ENGINE=MEMORY DEFAULT CHARSET=utf8 MAX_ROWS=100 CHECKSUM=1 ROW_FORMAT=FIXED COMMENT='Оперативные properties';

CREATE DATABASE IF NOT EXISTS `search` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `search`;

CREATE TABLE IF NOT EXISTS `permanent` (
  `stamp` bigint(13) unsigned NOT NULL DEFAULT '0',
  `upstring` varchar(260) NOT NULL DEFAULT '0'
) ENGINE=CSV DEFAULT CHARSET=utf8 ROW_FORMAT=FIXED COMMENT='постоянное хранение результатов поиска ';

CREATE TABLE IF NOT EXISTS `s1572532179212` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `upstring` varchar(260) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `upstring` (`upstring`),
  UNIQUE KEY `upstring_2` (`upstring`),
  UNIQUE KEY `upstring_3` (`upstring`),
  UNIQUE KEY `upstring_4` (`upstring`),
  UNIQUE KEY `upstring_5` (`upstring`),
  UNIQUE KEY `upstring_6` (`upstring`),
  UNIQUE KEY `upstring_7` (`upstring`),
  UNIQUE KEY `upstring_8` (`upstring`),
  UNIQUE KEY `upstring_9` (`upstring`),
  UNIQUE KEY `upstring_10` (`upstring`),
  UNIQUE KEY `upstring_11` (`upstring`),
  UNIQUE KEY `upstring_12` (`upstring`),
  UNIQUE KEY `upstring_13` (`upstring`),
  UNIQUE KEY `upstring_14` (`upstring`),
  UNIQUE KEY `upstring_15` (`upstring`),
  UNIQUE KEY `upstring_16` (`upstring`),
  UNIQUE KEY `upstring_17` (`upstring`),
  UNIQUE KEY `upstring_18` (`upstring`)
) ENGINE=MEMORY DEFAULT CHARSET=utf8 MAX_ROWS=100000;

CREATE TABLE IF NOT EXISTS `s1572532188840` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `upstring` varchar(260) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `upstring` (`upstring`)
) ENGINE=MEMORY DEFAULT CHARSET=utf8 MAX_ROWS=100000;

CREATE DATABASE IF NOT EXISTS `test` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `test`;

CREATE TABLE IF NOT EXISTS `build_gradle` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned zerofill NOT NULL DEFAULT '0442278000000',
  `upstring` varchar(1024) NOT NULL DEFAULT '1024 symbols max',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `upstring` (`upstring`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='g:\\My_Proj\\FtpClientPlus\\modules\\networker\\build.gradle';

CREATE TABLE IF NOT EXISTS `properties` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) unsigned NOT NULL,
  `squidans` varchar(20) NOT NULL,
  `bytes` int(11) NOT NULL,
  `timespend` int(11) NOT NULL DEFAULT '-666',
  `site` varchar(190) NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,
  KEY `site` (`site`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `test` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `stamp` bigint(13) NOT NULL DEFAULT '442278000000',
  `counter` int(11) NOT NULL DEFAULT '1',
  PRIMARY KEY (`idrec`),
  KEY `counter` (`counter`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `velkompc` (
  `idrec` mediumint(7) unsigned NOT NULL AUTO_INCREMENT,
  `NamePP` varchar(20) DEFAULT 'no name',
  `AddressPP` tinytext,
  `SegmentPP` varchar(200) DEFAULT 'unknown',
  `instr` varchar(20) DEFAULT 'not set',
  `OnlineNow` tinyint(1) DEFAULT '0',
  `userName` char(110) NOT NULL DEFAULT 'No user resolved',
  `TimeNow` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idrec`),
  KEY `NamePP` (`NamePP`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=FIXED COMMENT='Scan Stats for all time';

CREATE DATABASE IF NOT EXISTS `u0466446_properties` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `u0466446_properties`;

CREATE TABLE IF NOT EXISTS `general` (
  `idproperties` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) NOT NULL,
  `valueofproperty` varchar(250) DEFAULT 'value',
  `bin` mediumblob,
  `javaid` varchar(250) DEFAULT 'javaid miss',
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idproperties`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `mail` (
  `identry` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Номер записи',
  `javaid` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'all' COMMENT 'ID для JAVA-программы',
  `property` varchar(250) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Имя настройки',
  `valueofproperty` text COLLATE utf8_unicode_ci NOT NULL COMMENT 'Значение как String',
  `timeset` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Когда установлено',
  PRIMARY KEY (`identry`),
  UNIQUE KEY `javaid` (`javaid`,`property`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Настройки почтового клиента';

CREATE TABLE IF NOT EXISTS `ostpst` (
  `idproperties` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) NOT NULL,
  `valueofproperty` varchar(250) DEFAULT 'value',
  `bin` mediumblob,
  `javaid` varchar(250) DEFAULT 'javaid miss',
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idproperties`),
  UNIQUE KEY `idx_properties_new_property_javaid` (`property`,`javaid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `properties` (
  `idproperties` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) DEFAULT NULL,
  `valueofproperty` varchar(250) NOT NULL DEFAULT 'value',
  `javaid` varchar(250) DEFAULT NULL,
  `comment` longtext,
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `bins` mediumblob,
  PRIMARY KEY (`idproperties`),
  UNIQUE KEY `idx_properties_new_property_javaid` (`property`,`javaid`),
  FULLTEXT KEY `javaid` (`javaid`),
  FULLTEXT KEY `comment` (`comment`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `ru_vachok_checker` (
  `idproperties` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) NOT NULL,
  `valueofproperty` varchar(250) DEFAULT 'value',
  `javaid` varchar(250) DEFAULT 'javaid miss',
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idproperties`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `ru_vachok_ethosdistro` (
  `idproperties` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) NOT NULL,
  `valueofproperty` varchar(250) DEFAULT 'value',
  `javaid` varchar(250) DEFAULT 'javaid miss',
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idproperties`),
  UNIQUE KEY `idx_properties_new_property_javaid` (`property`,`javaid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `ru_vachok_inet` (
  `idproperties` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) NOT NULL,
  `valueofproperty` varchar(250) DEFAULT 'value',
  `javaid` varchar(250) DEFAULT 'javaid miss',
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idproperties`),
  UNIQUE KEY `idx_properties_new_property_javaid` (`property`,`javaid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `ru_vachok_inet_threads` (
  `idproperties` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) NOT NULL,
  `valueofproperty` varchar(250) DEFAULT 'value',
  `javaid` varchar(250) DEFAULT 'javaid miss',
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idproperties`),
  UNIQUE KEY `idx_properties_new_property_javaid` (`property`,`javaid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `ru_vachok_inet_threads_database` (
  `idproperties` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) NOT NULL,
  `valueofproperty` varchar(250) DEFAULT 'value',
  `javaid` varchar(250) DEFAULT 'javaid miss',
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idproperties`),
  UNIQUE KEY `idx_properties_new_property_javaid` (`property`,`javaid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `ru_vachok_money` (
  `idproperties` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) NOT NULL,
  `valueofproperty` varchar(250) DEFAULT 'value',
  `javaid` varchar(250) DEFAULT 'javaid miss',
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idproperties`),
  UNIQUE KEY `idx_properties_new_property_javaid` (`property`,`javaid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `ru_vachok_mysqlandprops` (
  `idproperties` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) NOT NULL,
  `valueofproperty` varchar(250) DEFAULT 'value',
  `javaid` varchar(250) DEFAULT 'javaid miss',
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idproperties`),
  UNIQUE KEY `idx_properties_new_property_javaid` (`property`,`javaid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `ru_vachok_mysqlandprops_props` (
  `idproperties` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) NOT NULL,
  `valueofproperty` varchar(250) DEFAULT 'value',
  `javaid` varchar(250) DEFAULT 'javaid miss',
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idproperties`),
  UNIQUE KEY `idx_properties_new_property_javaid` (`property`,`javaid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `ru_vachok_networker` (
  `idproperties` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) NOT NULL,
  `valueofproperty` varchar(250) DEFAULT NULL,
  `javaid` varchar(250) DEFAULT 'javaid miss',
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `stack` text NOT NULL COMMENT 'Stack вызова',
  PRIMARY KEY (`idproperties`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `ru_vachok_networker_last` (
  `idproperties` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) NOT NULL,
  `valueofproperty` varchar(250) DEFAULT NULL,
  `javaid` varchar(250) DEFAULT 'javaid miss',
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `stack` text NOT NULL COMMENT 'Stack вызова',
  PRIMARY KEY (`idproperties`),
  UNIQUE KEY `property` (`property`),
  UNIQUE KEY `idx_properties_new_property_javaid` (`property`,`javaid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `ru_vachok_parsers` (
  `idproperties` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) NOT NULL,
  `valueofproperty` varchar(250) DEFAULT 'value',
  `javaid` varchar(250) DEFAULT 'javaid miss',
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idproperties`),
  UNIQUE KEY `idx_properties_new_property_javaid` (`property`,`javaid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `ru_vachok_pbem_chess` (
  `idproperties` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) NOT NULL,
  `valueofproperty` varchar(250) DEFAULT 'value',
  `javaid` varchar(250) DEFAULT 'javaid miss',
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idproperties`),
  UNIQUE KEY `idx_properties_new_property_javaid` (`property`,`javaid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `ru_vachok_smbcli` (
  `idproperties` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) NOT NULL,
  `valueofproperty` varchar(250) DEFAULT NULL,
  `javaid` varchar(250) DEFAULT 'javaid miss',
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idproperties`),
  UNIQUE KEY `idx_properties_new_property_javaid` (`property`,`javaid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `ru_vachok_sr` (
  `idproperties` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) NOT NULL,
  `valueofproperty` varchar(250) DEFAULT 'value',
  `javaid` varchar(250) DEFAULT 'javaid miss',
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idproperties`),
  UNIQUE KEY `idx_properties_new_property_javaid` (`property`,`javaid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE DATABASE IF NOT EXISTS `u0466446_velkom` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `u0466446_velkom`;

CREATE TABLE IF NOT EXISTS `adusers` (
  `idrec` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID записи',
  `userDomain` varchar(254) NOT NULL DEFAULT 'eatmeat.ru',
  `userName` varchar(254) DEFAULT NULL,
  `userRealName` varchar(254) DEFAULT NULL,
  `userSurname` varchar(254) DEFAULT NULL,
  `distinguishedName` varchar(254) DEFAULT NULL,
  `userPrincipalName` varchar(254) DEFAULT NULL,
  `surname` varchar(254) DEFAULT NULL,
  `SID` varchar(254) DEFAULT NULL,
  `samAccountName` varchar(254) DEFAULT NULL,
  `objectClass` varchar(254) DEFAULT NULL,
  `objectGUID` varchar(254) DEFAULT NULL,
  `name` varchar(254) DEFAULT NULL,
  `enabled` varchar(254) DEFAULT NULL,
  `givenName` varchar(254) DEFAULT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `samUniq` (`samAccountName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Список пользователей Active Directory';

CREATE TABLE IF NOT EXISTS `common` (
  `idrec` int(11) NOT NULL AUTO_INCREMENT,
  `dir` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `user` varchar(255) NOT NULL,
  `users` text NOT NULL,
  `timerec` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idrec`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `inventory` (
  `iserid` int(11) NOT NULL AUTO_INCREMENT,
  `Surname` varchar(50) NOT NULL,
  `Name` varchar(50) DEFAULT NULL,
  `TimeLast` timestamp NULL DEFAULT NULL,
  `TimeUpd` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`iserid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `matrix` (
  `idrec` int(11) NOT NULL AUTO_INCREMENT,
  `﻿Otdel` varchar(200) NOT NULL DEFAULT 'Otdel',
  `Doljnost` text,
  `fullinet` int(11) DEFAULT NULL,
  `stdinet` bigint(20) DEFAULT NULL,
  `owaasync` int(11) DEFAULT NULL,
  `limitinet` bigint(20) DEFAULT NULL,
  `VPN` bigint(20) DEFAULT NULL,
  `sendmail` int(11) DEFAULT NULL,
  `changes` longblob,
  `filetype` varchar(5) DEFAULT NULL,
  `Surname` varchar(90) DEFAULT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `file` (`changes`(767),`filetype`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `oldfiles` (
  `idrec` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `AbsolutePath` varchar(255) NOT NULL,
  `size` float NOT NULL,
  `Attributes` text NOT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `AbsolutePath` (`AbsolutePath`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `pcuser` (
  `idRec` int(11) NOT NULL AUTO_INCREMENT,
  `pcName` varchar(200) NOT NULL DEFAULT 'noname',
  `userName` varchar(100) NOT NULL DEFAULT 'неопределён',
  `whenQueried` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idRec`),
  UNIQUE KEY `pc_user` (`pcName`,`userName`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='actual';

CREATE TABLE IF NOT EXISTS `velkompc` (
  `idrec` mediumint(7) unsigned NOT NULL AUTO_INCREMENT,
  `NamePP` varchar(20) DEFAULT NULL,
  `AddressPP` tinytext,
  `SegmentPP` varchar(200) DEFAULT NULL,
  `instr` varchar(20) DEFAULT NULL,
  `OnlineNow` tinyint(1) DEFAULT NULL,
  `userName` char(110) NOT NULL DEFAULT 'No user resolved',
  `TimeNow` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`idrec`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 MAX_ROWS=2500000 ROW_FORMAT=COMPRESSED COMMENT='Scan Stats for all time';

CREATE TABLE IF NOT EXISTS `worktime` (
  `recid` int(11) NOT NULL AUTO_INCREMENT,
  `Date` varchar(150) DEFAULT NULL,
  `Timein` bigint(20) NOT NULL,
  `Timeout` bigint(20) NOT NULL,
  PRIMARY KEY (`recid`),
  UNIQUE KEY `in` (`Date`,`Timein`),
  UNIQUE KEY `out` (`Date`,`Timeout`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='monitor';

CREATE DATABASE IF NOT EXISTS `velkom` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `velkom`;

CREATE TABLE IF NOT EXISTS `adusers` (
  `idrec` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID записи',
  `userDomain` varchar(254) NOT NULL DEFAULT 'eatmeat.ru',
  `userName` varchar(254),
  `userRealName` varchar(254),
  `userSurname` varchar(254),
  `distinguishedName` varchar(254),
  `userPrincipalName` varchar(254),
  `surname` varchar(254),
  `SID` varchar(254),
  `samAccountName` varchar(254),
  `objectClass` varchar(254),
  `objectGUID` varchar(254),
  `name` varchar(254),
  `enabled` varchar(254),
  `givenName` varchar(254),
  PRIMARY KEY (`idrec`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Список пользователей Active Directory';

CREATE TABLE `common` (
	`idrec` INT(11) NOT NULL,
	`dir` VARCHAR(255) NOT NULL COLLATE 'utf8_bin',
	`user` VARCHAR(255) NOT NULL COLLATE 'utf8_general_ci',
	`users` TEXT NOT NULL COLLATE 'utf8_general_ci',
	`timerec` DATETIME NOT NULL
) ENGINE=MyISAM;

DELIMITER //
CREATE DEFINER=`kudr`@`%` EVENT `del_trash_inet` ON SCHEDULE EVERY 35 MINUTE STARTS '2019-10-15 20:33:28' ON COMPLETION PRESERVE ENABLE COMMENT 'Удаление мусора из лога' DO BEGIN
DELETE  FROM velkom.inetstats WHERE site LIKE '%clients1.google%';
DELETE  FROM velkom.inetstats WHERE site LIKE '%g.ceipmsn.com%';
DELETE  FROM velkom.inetstats WHERE site LIKE '%ping3.teamviewer.com%';
DELETE  FROM velkom.inetstats WHERE site LIKE '%symcb.com%';
DELETE  FROM velkom.inetstats WHERE site LIKE '%entrust.net%';
DELETE  FROM velkom.inetstats WHERE site LIKE '%windowsupdate.com%';
DELETE  FROM velkom.inetstats WHERE site LIKE '%msftncsi%';
DELETE  FROM velkom.inetstats WHERE site LIKE '%globalsign.net%';
DELETE  FROM velkom.inetstats WHERE site LIKE '%msftncsi%';
SELECT @id := MAX(idrec) FROM inetstats;
INSERT INTO log.networker (classname, msgtype, msgvalue, pc) VALUES ('inet delete trash', 'info', @id, 'sql');
END//
DELIMITER ;

CREATE TABLE IF NOT EXISTS `general` (
  `idRec` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) NOT NULL,
  `valueofproperty` varchar(250) DEFAULT 'value',
  `bin` mediumblob,
  `javaid` varchar(250) DEFAULT 'javaid miss',
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idRec`),
  UNIQUE KEY `idx_properties_new_property_javaid` (`property`,`javaid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 MAX_ROWS=20 CHECKSUM=1 ROW_FORMAT=FIXED;

CREATE TABLE IF NOT EXISTS `inetstats` (
  `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT,
  `Date` bigint(13) unsigned NOT NULL COMMENT 'timestamp',
  `inte` int(11) unsigned NOT NULL COMMENT 'server ans in ms',
  `ip` varchar(20) NOT NULL COMMENT 'ip',
  `response` enum('TAG_NONE/400','TAG_NONE/503','TAG_NONE/505','TAG_NONE_ABORTED/000','TAG_NONE_TIMEDOUT/413','TCP_DENIED/403','TCP_DENIED_ABORTED/403','TCP_DENIED_REPLY/403','TCP_HIT/200','TCP_HIT/206','TCP_INM_HIT/304','TCP_MEM_HIT/200','TCP_MEM_HIT/206','TCP_MISS/200','TCP_MISS/204','TCP_MISS/206','TCP_MISS/301','TCP_MISS/302','TCP_MISS/304','TCP_MISS/307','TCP_MISS/400','TCP_MISS/401','TCP_MISS/403','TCP_MISS/404','TCP_MISS/405','TCP_MISS/502','TCP_MISS/503','TCP_MISS_ABORTED/000','TCP_MISS_ABORTED/200','TCP_MISS_ABORTED/206','TCP_MISS_ABORTED/403','TCP_MISS_TIMEDOUT/200','TCP_REFRESH_MODIFIED/200','TCP_REFRESH_UNMODIFIED/200','TCP_REFRESH_UNMODIFIED/304','TCP_TUNNEL/200') NOT NULL COMMENT 'squid',
  `bytes` int(11) unsigned NOT NULL COMMENT 'Кол-во байт в запросе',
  `method` enum('CONNECT','GET','HEAD','NONE','OPTIONS','POST','PUT') NOT NULL COMMENT 'http',
  `site` varchar(254) NOT NULL COMMENT 'url',
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `Date` (`Date`,`ip`,`bytes`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='113 rows added by rups00 at Thu Oct 31 19:48:59 MSK 2019';

CREATE TABLE IF NOT EXISTS `inventory` (
  `idRec` int(11) NOT NULL AUTO_INCREMENT,
  `Surname` varchar(50) NOT NULL,
  `Name` varchar(50) DEFAULT NULL,
  `TimeLast` timestamp NULL DEFAULT NULL,
  `TimeUpd` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`idRec`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `last50logs` (
	`idrec` INT(4) NOT NULL,
	`stamp` TIMESTAMP NOT NULL,
	`upstring` VARCHAR(9999) NOT NULL COLLATE 'utf8_general_ci',
	`classname` VARCHAR(209) NOT NULL COLLATE 'utf8_general_ci',
	`msgtype` VARCHAR(509) NOT NULL COLLATE 'utf8_general_ci',
	`msgvalue` VARCHAR(1999) NOT NULL COLLATE 'utf8_general_ci',
	`pc` VARCHAR(35) NOT NULL COLLATE 'utf8_general_ci',
	`stack` VARCHAR(9000) NOT NULL COLLATE 'utf8_general_ci'
) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS `matrix` (
  `idrec` int(11) NOT NULL AUTO_INCREMENT,
  `﻿Otdel` varchar(200) NOT NULL DEFAULT 'Otdel',
  `Doljnost` text,
  `fullinet` int(11) DEFAULT NULL,
  `stdinet` bigint(20) DEFAULT NULL,
  `owaasync` int(11) DEFAULT NULL,
  `limitinet` bigint(20) DEFAULT NULL,
  `VPN` bigint(20) DEFAULT NULL,
  `sendmail` int(11) DEFAULT NULL,
  `changes` longblob,
  `filetype` varchar(5) DEFAULT NULL,
  `Surname` varchar(90) DEFAULT NULL,
  PRIMARY KEY (`idrec`),
  UNIQUE KEY `file` (`changes`(767),`filetype`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `pcauto300` (
	`idRec` MEDIUMINT(6) UNSIGNED NOT NULL,
	`pcName` VARCHAR(20) NOT NULL COLLATE 'utf8_general_ci',
	`userName` VARCHAR(45) NOT NULL COLLATE 'utf8_general_ci',
	`lastmod` VARCHAR(50) NOT NULL COLLATE 'utf8_general_ci',
	`stamp` BIGINT(13) UNSIGNED NULL,
	`whenQueried` TIMESTAMP NOT NULL
) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS `pcuser` (
  `idRec` int(11) NOT NULL AUTO_INCREMENT,
  `pcName` varchar(20) NOT NULL DEFAULT 'noname',
  `userName` varchar(100) NOT NULL DEFAULT 'неопределён',
  `whenQueried` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastOnLine` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `On` int(5) NOT NULL DEFAULT '1',
  `Off` int(5) NOT NULL DEFAULT '1',
  `Total` int(5) DEFAULT '1',
  PRIMARY KEY (`idRec`),
  UNIQUE KEY `pc-user` (`pcName`,`userName`),
  UNIQUE KEY `UserUniq` (`pcName`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1;

CREATE TABLE IF NOT EXISTS `pcuserauto` (
  `idRec` mediumint(6) unsigned NOT NULL AUTO_INCREMENT,
  `pcName` varchar(20) NOT NULL DEFAULT 'noname',
  `userName` varchar(45) NOT NULL DEFAULT 'неопределён',
  `lastmod` varchar(50) NOT NULL DEFAULT 'not set',
  `stamp` bigint(13) unsigned DEFAULT NULL,
  `whenQueried` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idRec`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=FIXED;

CREATE TABLE IF NOT EXISTS `props` (
  `idRec` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) NOT NULL,
  `valueofproperty` varchar(250) DEFAULT NULL,
  `javaid` varchar(250) DEFAULT 'javaid miss',
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `stack` text COMMENT 'Stack вызова',
  PRIMARY KEY (`idRec`),
  UNIQUE KEY `idx_properties_new_property_javaid` (`property`,`javaid`),
  KEY `mem` (`property`,`valueofproperty`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=FIXED;

CREATE TABLE `propsview` (
	`idrec` INT(2) NOT NULL COMMENT 'id записи',
	`property` VARCHAR(50) NOT NULL COLLATE 'utf8_general_ci',
	`valueofproperty` VARCHAR(254) NOT NULL COLLATE 'utf8_general_ci',
	`timeSet` DATETIME NOT NULL,
	`setter` VARCHAR(50) NOT NULL COLLATE 'utf8_general_ci'
) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS `props_ro` (
  `idRec` int(11) NOT NULL AUTO_INCREMENT,
  `property` varchar(70) NOT NULL,
  `valueofproperty` varchar(250) DEFAULT NULL,
  `javaid` varchar(250) DEFAULT 'javaid miss',
  `timeset` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `stack` text NOT NULL COMMENT 'Stack ',
  PRIMARY KEY (`idRec`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 MAX_ROWS=2 CHECKSUM=1;

CREATE TABLE `slow_log` (
	`start_time` TIMESTAMP NOT NULL,
	`user_host` MEDIUMTEXT NOT NULL COLLATE 'utf8_general_ci',
	`query_time` TIME NOT NULL,
	`lock_time` TIME NOT NULL,
	`rows_sent` INT(11) NOT NULL,
	`rows_examined` INT(11) NOT NULL,
	`db` VARCHAR(512) NOT NULL COLLATE 'utf8_general_ci',
	`last_insert_id` INT(11) NOT NULL,
	`insert_id` INT(11) NOT NULL,
	`server_id` INT(10) UNSIGNED NOT NULL,
	`sql_text` MEDIUMTEXT NOT NULL COLLATE 'utf8_general_ci',
	`thread_id` BIGINT(21) UNSIGNED NOT NULL
) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS `velkompc` (
  `idrec` mediumint(7) unsigned NOT NULL AUTO_INCREMENT,
  `NamePP` varchar(20) DEFAULT 'no name',
  `AddressPP` tinytext,
  `SegmentPP` varchar(200) DEFAULT 'unknown',
  `instr` varchar(20) DEFAULT NULL,
  `OnlineNow` tinyint(1) DEFAULT '0',
  `userName` char(110) NOT NULL DEFAULT 'No user resolved',
  `TimeNow` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idrec`),
  KEY `NamePP` (`NamePP`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=FIXED COMMENT='Scan Stats for all time';

DROP TABLE IF EXISTS `common`;
CREATE ALGORITHM=UNDEFINED DEFINER=`kudr`@`%` SQL SECURITY DEFINER VIEW `common` AS select `common`.`common`.`idrec` AS `idrec`,`common`.`common`.`dir` AS `dir`,`common`.`common`.`user` AS `user`,`common`.`common`.`users` AS `users`,`common`.`common`.`timerec` AS `timerec` from `common`.`common` order by `common`.`common`.`timerec` desc limit 100;

DROP TABLE IF EXISTS `last50logs`;
CREATE ALGORITHM=UNDEFINED DEFINER=`kudr`@`%` SQL SECURITY DEFINER VIEW `last50logs` AS select `log`.`networker`.`idrec` AS `idrec`,`log`.`networker`.`stamp` AS `stamp`,`log`.`networker`.`upstring` AS `upstring`,`log`.`networker`.`classname` AS `classname`,`log`.`networker`.`msgtype` AS `msgtype`,`log`.`networker`.`msgvalue` AS `msgvalue`,`log`.`networker`.`pc` AS `pc`,`log`.`networker`.`stack` AS `stack` from `log`.`networker` order by `log`.`networker`.`idrec` desc limit 50;

DROP TABLE IF EXISTS `pcauto300`;
CREATE ALGORITHM=UNDEFINED DEFINER=`kudr`@`%` SQL SECURITY DEFINER VIEW `pcauto300` AS select `pcuserauto`.`idRec` AS `idRec`,`pcuserauto`.`pcName` AS `pcName`,`pcuserauto`.`userName` AS `userName`,`pcuserauto`.`lastmod` AS `lastmod`,`pcuserauto`.`stamp` AS `stamp`,`pcuserauto`.`whenQueried` AS `whenQueried` from `pcuserauto` order by `pcuserauto`.`idRec` desc limit 300;

DROP TABLE IF EXISTS `propsview`;
CREATE ALGORITHM=UNDEFINED DEFINER=`kudr`@`%` SQL SECURITY DEFINER VIEW `propsview` AS select `mem`.`properties`.`idrec` AS `idrec`,`mem`.`properties`.`property` AS `property`,`mem`.`properties`.`valueofproperty` AS `valueofproperty`,`mem`.`properties`.`timeSet` AS `timeSet`,`mem`.`properties`.`setter` AS `setter` from `mem`.`properties`;

DROP TABLE IF EXISTS `slow_log`;
CREATE ALGORITHM=UNDEFINED DEFINER=`kudr`@`%` SQL SECURITY DEFINER VIEW `slow_log` AS select `mysql`.`slow_log`.`start_time` AS `start_time`,`mysql`.`slow_log`.`user_host` AS `user_host`,`mysql`.`slow_log`.`query_time` AS `query_time`,`mysql`.`slow_log`.`lock_time` AS `lock_time`,`mysql`.`slow_log`.`rows_sent` AS `rows_sent`,`mysql`.`slow_log`.`rows_examined` AS `rows_examined`,`mysql`.`slow_log`.`db` AS `db`,`mysql`.`slow_log`.`last_insert_id` AS `last_insert_id`,`mysql`.`slow_log`.`insert_id` AS `insert_id`,`mysql`.`slow_log`.`server_id` AS `server_id`,`mysql`.`slow_log`.`sql_text` AS `sql_text`,`mysql`.`slow_log`.`thread_id` AS `thread_id` from `mysql`.`slow_log` order by `mysql`.`slow_log`.`start_time` desc limit 10;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
