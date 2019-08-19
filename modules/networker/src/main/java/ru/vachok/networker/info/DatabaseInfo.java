// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.accesscontrol.inetstats.InternetUse;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;


/**
 @since 13.08.2019 (17:15) */
public abstract class DatabaseInfo implements InformationFactory {
    
    
    static final String SQL_RESPONSE_TIME = "SELECT DISTINCT `inte` FROM `inetstats` WHERE `ip` LIKE ?";
    
    static final String SQL_BYTES = "SELECT `bytes` FROM `inetstats` WHERE `ip` LIKE ?";
    
    private static final MysqlDataSource MYSQL_DATA_SOURCE = new RegRuMysql().getDataSourceSchema(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    
    private static final MessageToUser messageToUser = new MessageLocal(DatabaseInfo.class.getSimpleName());
    
    public static int cleanedRows = 0;
    
    private static String aboutWhat = "null";
    
    public String getUserPCFromDB(String userName) {
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.TYPE_PCINFO);
        informationFactory.setClassOption(userName);
        return informationFactory.getInfo();
    }
    
    public String getCurrentPCUsers(String pcName) {
        throw new TODOException("16.08.2019 (10:56)");
    }
    
    @Contract("_ -> new")
    public static @NotNull DatabaseInfo getInfoInstance(String userOrPc) {
        DatabaseInfo.aboutWhat = userOrPc;
        if (new NameOrIPChecker(userOrPc).isLocalAddress()) {
            return new DatabasePCSearcher(userOrPc);
        }
        else {
            return new DatabaseUserSearcher(userOrPc);
        }
    }
    
    public static int cleanTrash() {
        int retInt = -1;
        for (String sqlLocal : UsefulUtilities.getDeleteTrashPatterns()) {
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
        cleanedRows = retInt;
        messageToUser.info(InternetUse.class.getSimpleName(), String.valueOf(retInt), "rows deleted.");
        return retInt;
    }
    
    public String getConnectStatistics() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(aboutWhat).append(" : ");
        long minutesResponse = TimeUnit.MILLISECONDS.toMinutes(DatabaseInfo.getInfoInstance(aboutWhat).getStatsFromDB(aboutWhat, SQL_RESPONSE_TIME, "inte"));
        stringBuilder.append(minutesResponse).append(" мин. (").append(String.format("%.02f", ((float) minutesResponse / (float) 60))).append(" ч.) время открытых сессий, ");
        stringBuilder.append(DatabaseInfo.getInfoInstance(aboutWhat).getStatsFromDB(aboutWhat, SQL_BYTES, ConstantsFor.SQLCOL_BYTES) / ConstantsFor.MBYTE)
            .append(" мегабайт трафика.");
        return stringBuilder.toString();
    }
    
    private long getStatsFromDB(String userCred, String sql, String colLabel) {
        long result = 0;
        try {
            InetAddress address = new NameOrIPChecker(userCred).resolveIP();
            userCred = address.getHostAddress();
        }
        catch (UnknownHostException e) {
            messageToUser.error(MessageFormat
                .format("DatabaseInfo.getStatsFromDB {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        try (Connection connection = MYSQL_DATA_SOURCE.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userCred);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        result = result + resultSet.getLong(colLabel);
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("DatabaseInfo.getStatsFromDB: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return result;
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        DatabaseInfo.aboutWhat = aboutWhat;
        throw new TODOException("16.08.2019 (10:47)");
    }
}
