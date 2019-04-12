package ru.vachok.networker.abstr;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.services.MessageLocal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 @since 02.04.2019 (10:24) */
public interface InternetUse {


    MysqlDataSource MYSQL_DATA_SOURCE = new RegRuMysql().getDataSourceSchema(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    String sql = "SELECT DISTINCT `Date`, `ip`, `response`, `method`, `site`, `bytes` FROM `inetstats` WHERE `ip` LIKE ? ORDER BY `inetstats`.`Date` DESC";

    String getUsage(String userCred);

    void showLog();

    default int cleanTrash() {
        MessageToUser messageToUser = new MessageLocal(InternetUse.class.getSimpleName());
        String sqlDel = "DELETE  FROM `inetstats` WHERE `site` LIKE '%clients1.google%'";
        try (Connection c = new RegRuMysql().getDataSourceSchema(ConstantsFor.DBBASENAME_U0466446_VELKOM).getConnection();
             PreparedStatement preparedStatement = c.prepareStatement(sqlDel)
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