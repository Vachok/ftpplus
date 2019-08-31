// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info.inet;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.user.UserInfo;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.info.inet.AccessLogUSERTest
 @since 17.08.2019 (15:19) */
class AccessLogUSER extends InternetUse {
    
    
    private static final String SQL_BYTES = "SELECT `bytes` FROM `inetstats` WHERE `ip` LIKE ?";
    
    private static final String SQL_RESPONSE_TIME = "SELECT `inte` FROM `inetstats` WHERE `ip` LIKE ?";
    
    private DataConnectTo dataConnectTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    
    private String aboutWhat;
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    private Map<Long, String> inetDateStampSite = new TreeMap<>();
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.aboutWhat = aboutWhat;
        return aboutWhat + " : " + getFromDB();
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
    
    @Override
    public void setOption(@NotNull Object option) {
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
    
    private long longFromDB(String sql, String colLabel) {
        long result = 0;
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                String checkIP = new NameOrIPChecker(aboutWhat).resolveInetAddress().getHostAddress();
                preparedStatement.setString(1, checkIP);
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
    
    private @NotNull String getUserStatistics() {
        StringBuilder stringBuilder = new StringBuilder();
        UserInfo userInfo = UserInfo.getInstance(UserInfo.ADUSER);
        userInfo.setOption(aboutWhat);
        stringBuilder.append(userInfo.getInfo()).append(" : ");
        long minutesResponse;
        long mbTraffic;
        float hoursResp;
        minutesResponse = TimeUnit.MILLISECONDS.toMinutes(longFromDB(SQL_RESPONSE_TIME, "inte"));
        stringBuilder.append(minutesResponse);
        
        hoursResp = (float) minutesResponse / (float) 60;
        stringBuilder.append(" мин. (").append(String.format("%.02f", hoursResp));
        stringBuilder.append(" ч.) время открытых сессий, ");
    
        mbTraffic = longFromDB(SQL_BYTES, ConstantsFor.SQLCOL_BYTES) / ConstantsFor.MBYTE;
        stringBuilder.append(mbTraffic);
        stringBuilder.append(" мегабайт трафика.");
        return stringBuilder.toString();
    }
    
    @Override
    public String writeObj(String logName, Object information) {
        logName = MessageFormat.format("{0}_{2}.{1}.log", this.getClass().getSimpleName(), aboutWhat, hashCode());
        return FileSystemWorker.writeFile(logName, (String) information);
    }
    
    @Override
    public int hashCode() {
        int result = dataConnectTo.hashCode();
        result = 31 * result + (aboutWhat != null ? aboutWhat.hashCode() : 0);
        result = 31 * result + (inetDateStampSite != null ? inetDateStampSite.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AccessLogUSER{");
        sb.append("dataConnectTo=").append(dataConnectTo);
        sb.append(", aboutWhat='").append(aboutWhat).append('\'');
        sb.append(", inetDateStampSite=").append(inetDateStampSite);
        sb.append('}');
        return sb.toString();
    }
    
    private void dbConnection() {
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
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
                .format("InetUserUserName.getFromDB {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
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
        
        if (!dataConnectTo.equals(user.dataConnectTo)) {
            return false;
        }
        if (aboutWhat != null ? !aboutWhat.equals(user.aboutWhat) : user.aboutWhat != null) {
            return false;
        }
        return inetDateStampSite != null ? inetDateStampSite.equals(user.inetDateStampSite) : user.inetDateStampSite == null;
    }
}