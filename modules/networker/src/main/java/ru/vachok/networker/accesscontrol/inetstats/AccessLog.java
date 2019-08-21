// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.PCInfo;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.accesscontrol.inetstats.AccessLogTest
 @since 17.08.2019 (15:19) */
public class AccessLog extends InternetUse {
    
    
    private static final String SQL_RESPONSE_TIME = "SELECT DISTINCT `inte` FROM `inetstats` WHERE `ip` LIKE ?";
    
    private static final String SQL_BYTES = "SELECT `bytes` FROM `inetstats` WHERE `ip` LIKE ?";
    
    private DataConnectTo dataConnectTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    
    private String aboutWhat;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private Map<Long, String> inetDateStampSite = new TreeMap<>();
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.aboutWhat = aboutWhat;
        InternetUse.getInetUse().setClassOption(aboutWhat);
        return getFromDB();
    }
    
    @Override
    public void setClassOption(@NotNull Object classOption) {
        this.aboutWhat = (String) classOption;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", AccessLog.class.getSimpleName() + "[\n", "\n]")
                .add("dataConnectTo = " + dataConnectTo)
                .add("aboutWhat = '" + aboutWhat + "'")
                .add("inetDateStampSite = " + inetDateStampSite)
                .toString();
    }
    
    @Override
    public String getInfo() {
        return getHTMLUsage(aboutWhat);
    }
    
    private @NotNull String getUserStatistics() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(aboutWhat).append(" : ");
        PCInfo pcInfo = PCInfo.getInstance(aboutWhat);
        long minutesResponse;
        long mbTraffic;
        float hoursResp;
        try {
            minutesResponse = TimeUnit.MILLISECONDS.toMinutes(pcInfo.getStatsFromDB(aboutWhat, SQL_RESPONSE_TIME, "inte"));
            stringBuilder.append(minutesResponse);
            
            hoursResp = (float) minutesResponse / (float) 60;
            stringBuilder.append(" мин. (").append(String.format("%.02f", hoursResp));
            stringBuilder.append(" ч.) время открытых сессий, ");
            
            mbTraffic = pcInfo.getStatsFromDB(aboutWhat, SQL_BYTES, ConstantsFor.SQLCOL_BYTES) / ConstantsFor.MBYTE;
            stringBuilder.append(mbTraffic);
            stringBuilder.append(" мегабайт трафика.");
            return stringBuilder.toString();
        }
        catch (UnknownHostException e) {
            return e.getMessage();
        }
    }
    
    private @NotNull String getFromDB() {
        String userPC = resolveUserPC();
        String conStat = getUserStatistics();
        userPC = MessageFormat.format("{2}\n<p>{0}:\n<br>{1}", userPC, conStat);
        return userPC;
    }
    
    @Contract(pure = true)
    private @NotNull String resolveUserPC() {
        if (new NameOrIPChecker(aboutWhat).isLocalAddress()) {
            try {
                String string = InetAddress.getByAddress(InetAddress.getByName(aboutWhat).getAddress()).toString();
                return string.replaceAll("\\Q/\\E", "");
            }
            catch (UnknownHostException e) {
                throw new InvokeIllegalException(this.getClass().getSimpleName());
            }
        }
        else {
            InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.USER);
            return informationFactory.getInfoAbout(aboutWhat);
        }
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