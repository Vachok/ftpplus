// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info.inetstats;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.info.inetstats.AccessLogTest
 @since 17.08.2019 (15:19) */
class AccessLog extends InternetUse {
    
    
    private DataConnectTo dataConnectTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    
    private String aboutWhat;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private Map<Long, String> inetDateStampSite = new TreeMap<>();
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.aboutWhat = aboutWhat;
        getI().setClassOption(aboutWhat);
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
        if(aboutWhat!=null&&!aboutWhat.isEmpty()) return getHTMLUsage(aboutWhat);
        else return MessageFormat.format("Identification is not set! \n<br>{0}", this);
    }
    
    private @NotNull String getFromDB() {
        String userPC = resolveUserPC();
        this.aboutWhat = userPC;
        String conStat = getUserStatistics();
        userPC = MessageFormat.format("{0}:\n<br>{1}", userPC, conStat);
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
            InformationFactory informationFactory = InformationFactory.getInstance(USER);
            return informationFactory.getInfoAbout(aboutWhat);
        }
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