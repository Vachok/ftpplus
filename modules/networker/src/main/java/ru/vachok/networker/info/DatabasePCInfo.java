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
 @see ru.vachok.networker.info.DatabasePCInfoTest
 @since 13.08.2019 (17:15) */
public abstract class DatabasePCInfo implements InformationFactory {
    
    
    private static final MysqlDataSource MYSQL_DATA_SOURCE = new RegRuMysql().getDataSourceSchema(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    
    private static final MessageToUser messageToUser = new MessageLocal(DatabasePCInfo.class.getSimpleName());
    
    public static int cleanedRows = 0;
    
    private static String aboutWhat = MessageFormat.format("{0}: Set the PC name!", DatabasePCInfo.class.getSimpleName());
    
    public static String getAboutWhat() {
        return aboutWhat;
    }
    
    public static void setAboutWhat(String aboutWhat) {
        DatabasePCInfo.aboutWhat = aboutWhat;
    }
    
    @Contract("_ -> new")
    public static @NotNull DatabasePCInfo getDatabaseInfo(String userOrPc) {
        DatabasePCInfo.aboutWhat = userOrPc;
        if (new NameOrIPChecker(userOrPc).isLocalAddress()) {
            return new DatabasePCPCSearcher(userOrPc);
        }
        else {
            return new DatabasePCUserSearcher(userOrPc);
        }
    }
    
    @Override
    public void setClassOption(Object classOption) {
        DatabasePCInfo.aboutWhat = (String) classOption;
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        DatabasePCInfo.aboutWhat = aboutWhat;
        return getUserByPCNameFromDB(aboutWhat);
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
        DatabasePCInfo.aboutWhat = pcName;
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.RESOLVER_PC_INFO);
        informationFactory.setClassOption(pcName);
        return informationFactory.getInfoAbout(pcName);
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", DatabasePCInfo.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}
