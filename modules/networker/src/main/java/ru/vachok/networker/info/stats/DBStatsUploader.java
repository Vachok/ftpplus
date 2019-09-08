package ru.vachok.networker.info.stats;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 @see DBStatsUploaderTest
 @since 08.09.2019 (10:08) */
public class DBStatsUploader implements Stats {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, DBStatsUploader.class.getSimpleName());
    
    private DataConnectTo dataConnectTo;
    
    private String aboutWhat = "";
    
    private List<String> valuesList = new ArrayList<>();
    
    @Contract(pure = true)
    public DBStatsUploader(DataConnectTo dataConnectTo) {
        this.dataConnectTo = dataConnectTo;
    }
    
    public DBStatsUploader() {
        this.dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
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
            stringBuilder.append(MessageFormat.format("DBStatsUploader.getInfo {0} - {1}", e.getClass().getTypeName(), e.getMessage()));
        }
        return stringBuilder.toString();
    }
    
    @Override
    public String getInfo() {
        if (aboutWhat.isEmpty() || valuesList.isEmpty()) {
            throw new IllegalArgumentException(aboutWhat);
        }
        uploadToTable(valuesList);
        return aboutWhat + " : " + uploadToTable(Arrays.asList(aboutWhat.split(","))) + " uploaded";
    }
    
    private int uploadToTable(@NotNull List<String> valuesArr) {
        int retInt;
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
    
    private long parseStamp(@NotNull String s) {
        String stringWithDate = "Thu Aug 01 05:38:48 MSK 2019";
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);
        String formatExpect = format.format(new Date());
        System.out.println("formatExpect = " + formatExpect);
        Date parsedDate = new Date();
        try {
            
            parsedDate = format.parse(stringWithDate);
            System.out.println("parsedDate = " + parsedDate);
        }
        catch (ParseException e) {
            messageToUser.error(e.getMessage() + " see line: 108 ***");
        }
        return parsedDate.getTime();
    }
    
    @Override
    public void setClassOption(Object option) {
        if (option instanceof DataConnectTo) {
            this.dataConnectTo = (DataConnectTo) option;
        }
        else if (option instanceof List) {
            this.valuesList = (List<String>) option;
        }
        else {
            this.aboutWhat = (String) option;
        }
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", DBStatsUploader.class.getSimpleName() + "[\n", "\n]")
            .add("dataConnectTo = " + dataConnectTo)
            .add("aboutWhat = '" + aboutWhat + "'")
            .add("valuesList = " + valuesList)
            .toString();
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