package ru.vachok.networker.data.synchronizer;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.*;
import java.text.*;
import java.util.Date;
import java.util.*;


/**
 @see DBStatsUploaderTest
 @since 08.09.2019 (10:08) */
public class DBStatsUploader implements Runnable {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, DBStatsUploader.class.getSimpleName());
    
    private DataConnectTo dataConnectTo;
    
    private String aboutWhat = "";
    
    private Object classOpt;
    
    @Contract(pure = true)
    public DBStatsUploader(DataConnectTo dataConnectTo) {
        this.dataConnectTo = dataConnectTo;
    }
    
    public DBStatsUploader() {
        this.dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
    }
    
    public void setClassOption(Object option) {
        if (option instanceof DataConnectTo) {
            this.dataConnectTo = (DataConnectTo) option;
        }
        else if (option instanceof List) {
            this.classOpt = option;
        }
        else {
            this.aboutWhat = (String) option;
        }
    }
    
    @Override
    public void run() {
        if (aboutWhat.isEmpty() || !(classOpt instanceof List)) {
            throw new IllegalArgumentException(aboutWhat);
        }
        messageToUser.info(MessageFormat.format("Upload: {0} rows to {1}", uploadToTable(), aboutWhat));
    }
    
    private int uploadToTable() {
        int retInt;
        List<String> valuesArr = (List<String>) classOpt;
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.STR_INETSTATS)) {
            try (PreparedStatement preparedStatement = connection
                .prepareStatement("insert into " + aboutWhat.replaceAll("\\Q.\\E", "_") + "(stamp, ip, bytes, site) values (?,?,?,?)")) {
                preparedStatement.setLong(1, parseStamp(valuesArr.get(0)));
                preparedStatement.setString(2, valuesArr.get(1));
                preparedStatement.setInt(3, Integer.parseInt(valuesArr.get(2)));
                preparedStatement.setString(4, valuesArr.get(3));
                retInt = preparedStatement.executeUpdate();
            }
            catch (IndexOutOfBoundsException e) {
                messageToUser.error(e.getMessage() + " see line: 163 ***");
                retInt = -163;
            }
        }
        catch (SQLException e) {
            String message = e.getMessage();
            if (!message.contains("Duplicate entry")) {
                messageToUser.error(message + " see line: 168 ***");
            }
            retInt = -168;
        }
        return retInt;
    }
    
    private long parseStamp(@NotNull String strToParse) {
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);
        Date parsedDate = new Date();
        try {
    
            parsedDate = format.parse(strToParse);
            System.out.println("parsedDate = " + parsedDate);
        }
        catch (ParseException e) {
            messageToUser.error(e.getMessage() + " see line: 108 ***");
        }
        return parsedDate.getTime();
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", DBStatsUploader.class.getSimpleName() + "[\n", "\n]")
            .add("dataConnectTo = " + dataConnectTo)
            .add("aboutWhat = '" + aboutWhat + "'")
                .add("valuesList = " + classOpt)
            .toString();
    }
    
    private String getInfoAbout(String aboutWhat) {
        this.aboutWhat = aboutWhat;
        StringBuilder stringBuilder = new StringBuilder();
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.STR_VELKOM);
             PreparedStatement preparedStatement = connection.prepareStatement("select * from " + aboutWhat.replaceAll("\\Q.\\E", "_"));
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                stringBuilder.append(resultSet.getInt("recid")).append(" ");
            }
        }
        catch (SQLException e) {
            stringBuilder.append(MessageFormat.format("DBStatsUploader.syncData {0} - {1}", e.getClass().getTypeName(), e.getMessage()));
        }
        return stringBuilder.toString();
    }
    
    int createUploadStatTable(String[] sql) {
        if (aboutWhat.contains(".")) {
            this.aboutWhat = aboutWhat.replaceAll("\\Q.\\E", "_");
        }
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.STR_INETSTATS)) {
            try (PreparedStatement preparedStatementCreateTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + aboutWhat + "(\n" +
                "  `idrec` mediumint(11) unsigned NOT NULL COMMENT '',\n" +
                "  `stamp` bigint(13) unsigned NOT NULL COMMENT '',\n" +
                "  `ip` varchar(20) NOT NULL COMMENT '',\n" +
                "  `bytes` int(11) NOT NULL COMMENT '',\n" +
                "  `timespend` int(11) NOT NULL DEFAULT '0',\n" +
                "  `site` varchar(254) NOT NULL COMMENT ''\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
            ) {
                if (preparedStatementCreateTable.executeUpdate() == 0) {
                    for (String sqlCom : sql) {
                        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlCom)) {
                            messageToUser.info("preparedStatement", aboutWhat, String.valueOf(preparedStatement.executeUpdate()));
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 142 ***");
            return -10;
        }
        return 0;
    }
}