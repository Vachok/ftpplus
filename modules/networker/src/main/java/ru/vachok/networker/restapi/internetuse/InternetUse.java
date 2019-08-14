// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.internetuse;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.accesscontrol.inetstats.InetIPUser;
import ru.vachok.networker.info.InformationFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;


/**
 @see ru.vachok.networker.restapi.internetuse.InternetUseTest
 @since 02.04.2019 (10:24) */
public interface InternetUse extends InformationFactory {
    
    
    MysqlDataSource MYSQL_DATA_SOURCE = new RegRuMysql().getDataSourceSchema(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    
    String SQL_SELECT_DIST = "SELECT DISTINCT `Date`, `ip`, `response`, `method`, `site`, `bytes` FROM `inetstats` WHERE `ip` LIKE ? ORDER BY `inetstats`.`Date` DESC";
    
    String SQL_RESPONSE_TIME = "SELECT DISTINCT `inte` FROM `inetstats` WHERE `ip` LIKE ?";
    
    String SQL_BYTES = "SELECT `bytes` FROM `inetstats` WHERE `ip` LIKE ?";
    
    String getUsage(String userCred);
    
    void showLog();
    
    String getConnectStatistics(String userCred);
    
    static int cleanTrash() {
        int retInt = -1;
        for (String sqlLocal : ConstantsFor.getDeleteTrashPatterns()) {
            try (Connection connection = MYSQL_DATA_SOURCE.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sqlLocal)
            ) {
                int retQuery = preparedStatement.executeUpdate();
                retInt = retInt + retQuery;
            }
            catch (SQLException e) {
                retInt = e.getErrorCode();
                System.err.println(MessageFormat.format("InternetUse.cleanTrash: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            }
        }
        return retInt;
    }
    
    @Override
    default String getInfoAbout(String aboutWhat) {
        try {
            InetAddress inetAddress = new NameOrIPChecker(aboutWhat).resolveIP();
            aboutWhat = inetAddress.getHostAddress();
        }
        catch (UnknownHostException e) {
            return MessageFormat.format("InternetUse.getInfoAbout: {0}, ({1})", e.getMessage(), e.getClass().getName());
        }
        return new InetIPUser().getUsage(aboutWhat);
    }
}
