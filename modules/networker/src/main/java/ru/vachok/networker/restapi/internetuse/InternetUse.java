// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.internetuse;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 @since 02.04.2019 (10:24) */
public interface InternetUse {

    MysqlDataSource MYSQL_DATA_SOURCE = new RegRuMysql().getDataSourceSchema(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    
    String SQL_SELECT_DIST = "SELECT DISTINCT `Date`, `ip`, `response`, `method`, `site`, `bytes` FROM `inetstats` WHERE `ip` LIKE ? ORDER BY `inetstats`.`Date` DESC";
    
    String SQL_DEL_CLIENTS1GOOGLE = "DELETE  FROM `inetstats` WHERE `site` LIKE '%clients1.google%'";
    
    String SQL_DEL_gceipmsncom = "DELETE  FROM `inetstats` WHERE `site` LIKE '%g.ceipmsn.com%'";

    String getUsage(String userCred);

    void showLog();
    
    default int cleanTrash(String sql) {
        MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
        try (Connection c = MYSQL_DATA_SOURCE.getConnection();
             PreparedStatement preparedStatement = c.prepareStatement(sql)
        ) {
            int retQuery = preparedStatement.executeUpdate();
            messageToUser.info(getClass().getSimpleName() + ".cleanTrash" , "deleting: " , " = " + retQuery);
            return retQuery;
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage());
            return -1;
        }
    }
}
