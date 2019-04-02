package ru.vachok.networker.accesscontrol.inetstats;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.mysqlandprops.RegRuMysql;


/**
 @since 02.04.2019 (10:24) */
public interface InternetUse {
    
    
    static final MysqlDataSource MYSQL_DATA_SOURCE = new RegRuMysql().getDataSourceSchema("u0466446_velkom");
    static final String sql = "SELECT DISTINCT `Date`, `ip`, `response`, `method`, `site`, `bytes` FROM `inetstats` WHERE `ip` LIKE ?";
    
    String getUsage(String userCred);
    
}
