package ru.vachok.networker.accesscontrol.inetstats;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;


/**
 @since 02.04.2019 (10:24) */
public interface InternetUse {


    MysqlDataSource MYSQL_DATA_SOURCE = new RegRuMysql().getDataSourceSchema(ConstantsFor.DBDASENAME_U0466446_VELKOM);
    String sql = "SELECT DISTINCT `Date`, `ip`, `response`, `method`, `site`, `bytes` FROM `inetstats` WHERE `ip` LIKE ? ORDER BY `inetstats`.`Date` DESC";

    String getUsage(String userCred);

    void showLog();
}
