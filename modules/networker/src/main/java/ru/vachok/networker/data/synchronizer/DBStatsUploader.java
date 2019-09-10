package ru.vachok.networker.data.synchronizer;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Deque;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 @see DBStatsUploaderTest
 @since 08.09.2019 (10:08) */
class DBStatsUploader extends SyncData {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, DBStatsUploader.class.getSimpleName());
    
    private String ipAddr = "";
    
    private String[] classOpt;
    
    private Deque<String> fromFileToJSON = new ConcurrentLinkedDeque<>();
    
    @Override
    public String syncData() {
        if (ipAddr.isEmpty() || ipAddr.matches(String.valueOf(ConstantsFor.PATTERN_IP))) {
            throw new IllegalArgumentException(MessageFormat.format("IP {0} is null or illegal ", ipAddr));
        }
        return MessageFormat.format("Upload: {0} rows to {1}", uploadToTable(), ipAddr);
    }
    
    @Override
    public void setOption(Object option) {
        if (option instanceof String[]) {
            this.classOpt = (String[]) option;
        }
        else if (option instanceof Deque) {
            this.fromFileToJSON = (Deque<String>) option;
        }
        else {
            this.ipAddr = (String) option;
        }
    }
    
    private long parseStamp(@NotNull String strToParse) {
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);
        Date parsedDate = new Date();
        try {
            
            parsedDate = format.parse(strToParse);
            System.out.println("parsedDate = " + parsedDate);
        }
        catch (ParseException e) {
            messageToUser.error(e.getMessage() + " see line: 76 ***");
        }
        return parsedDate.getTime();
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", DBStatsUploader.class.getSimpleName() + "[\n", "\n]")
            .add("aboutWhat = '" + ipAddr + "'")
            .add("classOpt = " + classOpt)
            .add("fromFileToJSON = " + fromFileToJSON)
            .add("CONNECT_TO = " + CONNECT_TO_LOCAL.getDataSource().getURL())
            .toString();
    }
    
    private int uploadToTable() {
        int retInt;
        String[] valuesArr = classOpt;
        try (Connection connection = CONNECT_TO_LOCAL.getDefaultConnection(ConstantsFor.STR_INETSTATS)) {
            try (PreparedStatement preparedStatement = connection
                .prepareStatement("insert into " + ipAddr.replaceAll("\\Q.\\E", "_") + "(stamp, ip, bytes, site) values (?,?,?,?)")) {
                preparedStatement.setLong(1, parseStamp(valuesArr[0]));
                preparedStatement.setString(2, valuesArr[1]);
                preparedStatement.setInt(3, Integer.parseInt(valuesArr[2]));
                preparedStatement.setString(4, valuesArr[3]);
                retInt = preparedStatement.executeUpdate();
            }
            catch (IndexOutOfBoundsException e) {
                messageToUser.error(e.getMessage() + " see line: 53 ***");
                retInt = -53;
            }
        }
        catch (SQLException e) {
            String message = e.getMessage();
            if (!message.contains("Duplicate entry")) {
                messageToUser.error(e.getMessage() + " see line: 60 ***");
            }
            retInt = -60;
        }
        return retInt;
    }
}