// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.internetuse;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 @see ru.vachok.networker.restapi.internetuse.InternetUseTest
 @since 02.04.2019 (10:24) */
public interface InternetUse {
    
    
    MysqlDataSource MYSQL_DATA_SOURCE = new RegRuMysql().getDataSourceSchema(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    
    String SQL_SELECT_DIST = "SELECT DISTINCT `Date`, `ip`, `response`, `method`, `site`, `bytes` FROM `inetstats` WHERE `ip` LIKE ? ORDER BY `inetstats`.`Date` DESC";
    
    String SQL_RESPONSE_TIME = "SELECT DISTINCT `inte` FROM `inetstats` WHERE `ip` LIKE ?";
    
    String SQL_BYTES = "SELECT `bytes` FROM `inetstats` WHERE `ip` LIKE ?";
    
    String getUsage(String userCred);
    
    void showLog();
    
    String getConnectStatistics(String userCred);
    
    default int cleanTrash() {
        int retInt = -1;
        for (String sqlLocal : ConstantsFor.getDeleteTrashPatterns()) {
            try (Connection c = MYSQL_DATA_SOURCE.getConnection();
                 PreparedStatement preparedStatement = c.prepareStatement(sqlLocal)
            ) {
                int retQuery = preparedStatement.executeUpdate();
                retInt = retInt + retQuery;
            }
            catch (SQLException e) {
                retInt = e.getErrorCode();
            }
        }
        return retInt;
    }
}
