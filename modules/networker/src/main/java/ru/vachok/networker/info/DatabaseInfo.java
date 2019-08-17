// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.accesscontrol.inetstats.InternetUse;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.StringJoiner;


/**
 @see ru.vachok.networker.info.DatabaseInfoTest
 @since 13.08.2019 (17:15) */
public abstract class DatabaseInfo implements InformationFactory {
    
    
    private static final MysqlDataSource MYSQL_DATA_SOURCE = new RegRuMysql().getDataSourceSchema(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    
    private static final MessageToUser messageToUser = new MessageLocal(DatabaseInfo.class.getSimpleName());
    
    public static int cleanedRows = 0;
    
    @Override
    public void setClassOption(Object classOption) {
        DatabaseInfo.aboutWhat = (String) classOption;
    }
    
    private static String aboutWhat = MessageFormat.format("{0}: Set the PC name!", DatabaseInfo.class.getSimpleName());
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        DatabaseInfo.aboutWhat = aboutWhat;
        return getUserByPCNameFromDB(aboutWhat);
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
    
    public long getStatsFromDB(String userCred, String sql, String colLabel) throws UnknownHostException {
        long result = 0;
        InetAddress address = InetAddress.getByName(userCred);
        userCred = address.getHostAddress();
        try (Connection connection = MYSQL_DATA_SOURCE.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userCred);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        result = result + resultSet.getLong(colLabel);
                    }
                    return result;
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("DatabaseInfo.getStatsFromDB: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return -1;
        }
    }
    
    public String getUserByPCNameFromDB(String pcName) {
        DatabaseInfo.aboutWhat = pcName;
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.TYPE_PCINFO);
        informationFactory.setClassOption(pcName); //fixme 17.08.2019 (16:21)
        return informationFactory.getInfoAbout(pcName);
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", DatabaseInfo.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}
