package ru.vachok.networker.ad.inet;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Date;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 @see UserReportsMakerTest
 @since 14.10.2019 (11:40) */
public class UserReportsMaker extends InternetUse {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, UserReportsMaker.class.getSimpleName());
    
    private String userCred;
    
    UserReportsMaker(String type) {
        this.userCred = type;
    }
    
    @Override
    public String getInfoAbout(String fileName) {
        Map<Date, String> dateStringMap = getMapUsage();
        File outFile = new File(fileName);
        List<String> datesList = new ArrayList<>();
        dateStringMap.keySet().forEach(date->{
            datesList.add(String.valueOf(date.getTime()));
        });
        Collections.sort(datesList);
        for (String dateLong : datesList) {
            Date keyDate = new Date(Long.parseLong(dateLong));
            FileSystemWorker.appendObjectToFile(outFile, keyDate + "," + dateStringMap.get(keyDate));
        }
        return outFile.toPath().toAbsolutePath().normalize().toString();
    }
    
    @Override
    public void setClassOption(@NotNull Object option) {
        this.userCred = (String) option;
    }
    
    @Override
    public String getInfo() {
        return AbstractForms.fromArray(getMapUsage());
    }
    
    private Map<Date, String> getMapUsage() {
        DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
        Map<Date, String> timeSite = new ConcurrentHashMap<>();
        this.userCred = resolveTableName();
        final String sql = createDBQuery();
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DB_INETSTATS + userCred);
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                timeSite.put(new Date(resultSet.getLong(ConstantsFor.DBCOL_STAMP)), MessageFormat
                        .format("{0} bytes: {1}", resultSet.getString("site"), resultSet.getInt(ConstantsFor.DBCOL_BYTES)));
            }
        }
        catch (SQLException e) {
            messageToUser.error("UserReportsMaker", "getMapUsage", e.getMessage() + " see line: 62");
        }
        messageToUser.info(this.getClass().getSimpleName(), "Returning MAP: ", timeSite.size() + " records");
        return timeSite;
    }
    
    private String resolveTableName() {
        if (userCred.contains(".")) {
            return userCred.replaceAll("\\Q.\\E", "_");
        }
        else {
            return userCred;
        }
    }
    
    private String createDBQuery() {
        return "SELECT * FROM inetstats." + userCred + " WHERE squidans NOT IN ('TCP_DENIED/403');";
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserReportsMaker{");
        sb.append("userCred='").append(userCred).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
