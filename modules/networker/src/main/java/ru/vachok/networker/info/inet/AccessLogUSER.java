// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info.inet;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.user.UserInfo;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.info.inet.AccessLogUSERTest
 @since 17.08.2019 (15:19) */
class AccessLogUSER extends InternetUse {
    
    
    private DataConnectTo dataConnectTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    
    private String aboutWhat;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private Map<Long, String> inetDateStampSite = new TreeMap<>();
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.aboutWhat = aboutWhat;
        return aboutWhat + " : " + getFromDB();
    }
    
    private @NotNull String getFromDB() {
        String userPC = resolveUserPC();
        dbConnection(userPC);
        String conStat = getUserStatistics();
        userPC = MessageFormat.format("{0}:\n<br>{1}", userPC, conStat);
        return userPC;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", AccessLogUSER.class.getSimpleName() + "[\n", "\n]")
                .add("dataConnectTo = " + dataConnectTo)
                .add("aboutWhat = '" + aboutWhat + "'")
                .add("inetDateStampSite = " + inetDateStampSite)
                .toString();
    }
    
    @Override
    public String fillWebModel() {
        return getHTMLUsage(aboutWhat);
    }
    
    @Override
    public String fillAttribute(String attributeName) {
        this.aboutWhat = attributeName;
        return getUserStatistics();
    }
    
    @Override
    public void setClassOption(@NotNull Object classOption) {
        writeLog("logName", new TForms().fromArray(Thread.currentThread().getStackTrace()));
        this.aboutWhat = (String) classOption;
    }
    
    @Override
    public String writeLog(String logName, String information) {
        logName = this.getClass().getSimpleName() + ".setClassOption.log";
        information = new TForms().fromArray(Thread.currentThread().getStackTrace());
        String logFile = FileSystemWorker.writeFile(logName, information);
        messageToUser.info(logFile);
        return logFile;
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
        return inetDateStampSite.equals(user.inetDateStampSite);
    }
    
    @Override
    public int hashCode() {
        int result = dataConnectTo.hashCode();
        result = 31 * result + (aboutWhat != null ? aboutWhat.hashCode() : 0);
        result = 31 * result + inetDateStampSite.hashCode();
        return result;
    }
    
    @Override
    public String getInfo() {
        if (aboutWhat != null && !aboutWhat.isEmpty()) {
            return getUserStatistics();
        }
        else return MessageFormat.format("Identification is not set! \n<br>{0}", this);
    }
    
    private String resolveUserPC() {
        UserInfo userInfo = UserInfo.getI(UserInfo.ADUSER);
        userInfo.setClassOption(aboutWhat);
        String info = userInfo.getInfo();
        return info;
    }
    
    private @NotNull String getUserStatistics() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(aboutWhat).append(" : ");
        long minutesResponse;
        long mbTraffic;
        float hoursResp;
        minutesResponse = TimeUnit.MILLISECONDS.toMinutes(InternetUse.getResponseTimeMs(aboutWhat));
        stringBuilder.append(minutesResponse);
        
        hoursResp = (float) minutesResponse / (float) 60;
        stringBuilder.append(" мин. (").append(String.format("%.02f", hoursResp));
        stringBuilder.append(" ч.) время открытых сессий, ");
    
        mbTraffic = InternetUse.getTrafficBytes(aboutWhat) / ConstantsFor.MBYTE;
        stringBuilder.append(mbTraffic);
        stringBuilder.append(" мегабайт трафика.");
        return stringBuilder.toString();
    }
    
    private void dbConnection(String userPC) {
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `inetstats` WHERE `ip` LIKE ?")) {
                preparedStatement.setString(1, userPC);
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
}