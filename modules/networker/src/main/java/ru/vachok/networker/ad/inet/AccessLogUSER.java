// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.inet;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.user.UserInfo;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.ad.inet.AccessLogUSERTest
 @since 17.08.2019 (15:19) */
class AccessLogUSER extends InternetUse {
    
    
    private static final String DB_VELKOMINETSTATS = "velkom.inetstats";
    
    private static final String SQL_BYTES = "SELECT `bytes` FROM `inetstats` WHERE `ip` LIKE ?";
    
    private static final String SQL_RESPONSE_TIME = "SELECT `inte` FROM `inetstats` WHERE `ip` LIKE ?";
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, AccessLogUSER.class.getSimpleName());
    
    private String aboutWhat;
    
    private Map<Long, String> inetDateStampSite = new TreeMap<>();
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.aboutWhat = aboutWhat;
        return getFromDB();
    }
    
    @Override
    public void setClassOption(@NotNull Object option) {
        writeObj("logName", new TForms().fromArray(Thread.currentThread().getStackTrace()));
        this.aboutWhat = (String) option;
    }
    
    @Override
    public String getInfo() {
        if (aboutWhat != null && !aboutWhat.isEmpty()) {
            return getUserStatistics();
        }
        else {
            return MessageFormat.format("Identification is not set! \n<br>{0}", this);
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AccessLogUSER{");
        sb.append("messageToUser=").append(messageToUser);
        sb.append(", inetDateStampSite=").append(inetDateStampSite);
        sb.append(", aboutWhat='").append(aboutWhat).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public String writeObj(String logName, Object information) {
        logName = MessageFormat.format("{0}_{2}.{1}.log", this.getClass().getSimpleName(), aboutWhat, hashCode());
        return FileSystemWorker.writeFile(logName, (String) information);
    }
    
    @Override
    public int hashCode() {
        int result = aboutWhat != null ? aboutWhat.hashCode() : 0;
        result = 31 * result + messageToUser.hashCode();
        result = 31 * result + inetDateStampSite.hashCode();
        return result;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        AccessLogUSER user = (AccessLogUSER) o;
        
        if (aboutWhat != null ? !aboutWhat.equals(user.aboutWhat) : user.aboutWhat != null) {
            return false;
        }
        if (!messageToUser.equals(user.messageToUser)) {
            return false;
        }
        return inetDateStampSite.equals(user.inetDateStampSite);
    }
    
    private @NotNull String getFromDB() {
        if (!new NameOrIPChecker(aboutWhat).isLocalAddress()) {
            setAboutWhatAsLocalIP();
        }
        dbConnection();
        return getUserStatistics();
    }
    
    private void setAboutWhatAsLocalIP() {
        UserInfo userInfo = UserInfo.getInstance(aboutWhat);
        this.aboutWhat = userInfo.getInfo();
    }
    
    private @NotNull String getUserStatistics() {
        StringBuilder stringBuilder = new StringBuilder();
        InformationFactory userInfo = InformationFactory.getInstance(ModelAttributeNames.ADUSER);
        userInfo.setClassOption(aboutWhat);
        stringBuilder.append(userInfo.getInfo());
        long minutesResponse;
        long mbTraffic;
        float hoursResp;
        minutesResponse = TimeUnit.MILLISECONDS.toMinutes(longFromDB(SQL_RESPONSE_TIME, "inte"));
        stringBuilder.append(minutesResponse);
        
        hoursResp = (float) minutesResponse / (float) 60;
        stringBuilder.append(" мин. (").append(String.format("%.02f", hoursResp));
        stringBuilder.append(" ч.) время открытых сессий, ");
    
        mbTraffic = longFromDB(SQL_BYTES, ConstantsFor.DBCOL_BYTES) / ConstantsFor.MBYTE;
        stringBuilder.append(mbTraffic);
        stringBuilder.append(" мегабайт трафика.");
        return stringBuilder.toString();
    }
    
    private long longFromDB(String sql, String colLabel) {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        long result = 0;
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(DB_VELKOMINETSTATS)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                String checkIP = new NameOrIPChecker(aboutWhat).resolveInetAddress().getHostAddress();
                preparedStatement.setString(1, checkIP);
                preparedStatement.setQueryTimeout(30);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        result = result + resultSet.getLong(colLabel);
                    }
                    return result;
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("AccessLogUSER.longFromDB: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return -1;
        }
    }
    
    private void dbConnection() {
    
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(DB_VELKOMINETSTATS)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `inetstats` WHERE `ip` LIKE ?")) {
                String checkIP = new NameOrIPChecker(aboutWhat).resolveInetAddress().getHostAddress();
                preparedStatement.setString(1, checkIP);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        inetDateStampSite.put(resultSet.getLong("Date"), resultSet.getString("site"));
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat
                    .format("AccessLogUSER.dbConnection {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
    }
    
}