CREATE TABLE IF NOT EXISTS log.networker(
  `idrec` mediumint(11) unsigned NOT NULL,
  `stamp` bigint(13) unsigned NOT NULL,
  `upstring` varchar(255) NOT NULL,
  `classname` varchar(255) NOT NULL,
  `msgtype` text,
  `msgvalue` text,
  `pc` varchar(50) NOT NULL,
  `stack` mediumtext
) ENGINE=MyIsam DEFAULT CHARSET=utf8;