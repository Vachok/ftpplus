package ru.vachok.networker.data.synchronizer;


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
class DBStatsUploader extends SyncData {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, DBStatsUploader.class.getSimpleName());
    
    private DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
    
    private String aboutWhat = "";
    
    private Object classOpt;
    
    @Override
    public String syncData() {
        if (aboutWhat.isEmpty() || !(classOpt instanceof List)) {
            throw new IllegalArgumentException(aboutWhat);
        }
        return MessageFormat.format("Upload: {0} rows to {1}", uploadToTable(), aboutWhat);
    }
    
    @Override
    public void setOption(Object option) {
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
            .add("dataConnectTo = " + dataConnectTo.getDataSource().getURL())
            .add("aboutWhat = '" + aboutWhat + "'")
                .add("valuesList = " + classOpt)
            .toString();
    }
    
    private @NotNull String getInfoAbout(String aboutWhat) {
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
}