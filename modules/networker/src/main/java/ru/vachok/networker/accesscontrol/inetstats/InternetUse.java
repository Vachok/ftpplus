// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.ad.PCUserNameHTMLResolver;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.info.HTMLGeneration;
import ru.vachok.networker.info.PCInfo;
import ru.vachok.networker.info.PageGenerationHelper;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.statistics.Stats;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.accesscontrol.inetstats.InternetUseTest
 @since 02.04.2019 (10:24) */
public abstract class InternetUse extends Stats implements Callable<Object> {
    
    
    static final String SQL_RESPONSE_TIME = "SELECT DISTINCT `inte` FROM `inetstats` WHERE `ip` LIKE ?";
    
    static final String SQL_BYTES = "SELECT `bytes` FROM `inetstats` WHERE `ip` LIKE ?";
    
    public static final MysqlDataSource MYSQL_DATA_SOURCE = new RegRuMysql().getDataSourceSchema(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    
    private static int cleanedRows = countCleanedRows();
    
    protected static String aboutWhat = "null";
    
    private static MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.DB, InternetUse.class.getSimpleName());
    
    private StringBuilder stringBuilder;
    
    private Map<String, String> siteResponseMap = new ConcurrentHashMap<>();
    
    private List<String> toWriteDenied = new ArrayList<>();
    
    private List<String> toWriteAllowed = new ArrayList<>();
    
    public static @NotNull String getUserStatistics(String aboutWhat) {
        InternetUse.aboutWhat = aboutWhat;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(aboutWhat).append(" : ");
        long minutesResponse;
        long mbTraffic;
        float hoursResp;
        try {
            minutesResponse = TimeUnit.MILLISECONDS.toMinutes(new PCUserNameHTMLResolver(aboutWhat).getStatsFromDB(aboutWhat, SQL_RESPONSE_TIME, "inte"));
            stringBuilder.append(minutesResponse);
            hoursResp = (float) minutesResponse / (float) 60;
            stringBuilder.append(" мин. (").append(String.format("%.02f", hoursResp));
            stringBuilder.append(" ч.) время открытых сессий, ");
            mbTraffic = new PCUserNameHTMLResolver(aboutWhat).getStatsFromDB(aboutWhat, SQL_BYTES, ConstantsFor.SQLCOL_BYTES) / ConstantsFor.MBYTE;
            stringBuilder.append(mbTraffic);
            stringBuilder.append(" мегабайт трафика.");
            return stringBuilder.toString();
        }
        catch (UnknownHostException e) {
            return e.getMessage();
        }
    }
    
    @Contract(" -> new")
    public static @NotNull InternetUse getInetUse() {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(aboutWhat);
            messageToUser.info(inetAddress.getHostAddress(), InetIPUser.class.getSimpleName() + " over " + InternetUse.class.getSimpleName(), inetAddress.getHostName());
            return new InetIPUser();
        }
        catch (UnknownFormatConversionException | UnknownHostException e) {
            messageToUser.info(aboutWhat, InetUserPCName.class.getSimpleName() + " over " + InternetUse.class.getSimpleName(), e.getMessage());
            return new InetUserPCName();
        }
    }
    
    @Override
    public Object call() {
        return cleanTrash();
    }
    
    private static int cleanTrash() {
        int retInt = -1;
        for (String sqlLocal : UsefulUtilities.getDeleteTrashPatterns()) {
            try (Connection connection = MYSQL_DATA_SOURCE.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sqlLocal)
            ) {
                int retQuery = preparedStatement.executeUpdate();
                retInt = retInt + retQuery;
            }
            catch (SQLException e) {
                retInt = e.getErrorCode();
                System.err.println(MessageFormat.format("InternetUse.cleanTrash: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            }
        }
        cleanedRows = retInt;
        messageToUser.info(InternetUse.class.getSimpleName(), String.valueOf(retInt), "rows deleted.");
        return retInt;
    }
    
    public String getInfoAbout(String aboutWhat) {
        InternetUse.aboutWhat = aboutWhat;
        return getInfo();
    }
    
    @Override
    public void setClassOption(@NotNull Object classOption) {
        InternetUse.aboutWhat = (String) classOption;
    }
    
    @Override
    public String getInfo() {
        return new InetUserUserName().getInfoAbout(aboutWhat);
    }
    
    @Override
    public String writeLog(String logName, String information) {
        return FileSystemWorker.writeFile(logName, information);
    }
    
    @NotNull String getUsage0(String userCred) {
        this.stringBuilder = new StringBuilder();
        stringBuilder.append("<details><summary>Посмотреть сайты (BETA)</summary>");
        stringBuilder.append("Показаны только <b>уникальные</b> сайты<br>");
        stringBuilder.append(InternetUse.countCleanedRows()).append(" trash rows cleaned<p>");
        
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(PCInfo.SQL_SELECT_DIST)) {
                preparedStatement.setString(1, userCred);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        resultSetWhileNext(resultSet);
                    }
                    if (resultSet.last()) {
                        LocalDate localDate = LocalDateTime.ofEpochSecond((resultSet.getLong("Date") / 1000), 0, ZoneOffset.ofHours(3)).toLocalDate();
                        int compareTo = LocalDate.now().compareTo(localDate);
                        stringBuilder.append("<b>Статистика за ").append(compareTo).append(" дней</b><br>");
                    }
                    if (resultSet.wasNull()) {
                        stringBuilder.append("No usage detected");
                    }
                    makeReadableResults();
                }
            }
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder.toString();
    }
    
    @Contract(pure = true)
    private static int countCleanedRows() {
        AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor()
            .scheduleWithFixedDelay(InternetUse::cleanTrash, 0, UsefulUtilities.getDelay(), TimeUnit.MINUTES);
        return cleanedRows;
    }
    
    private void resultSetWhileNext(@NotNull ResultSet r) throws SQLException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss z, E");
        String date = dateFormat.format(new Date(r.getLong("Date")));
        
        String siteString = r.getString("site");
        try {
            String[] noPref = siteString.split("//");
            siteString = noPref[1].split("/")[0];
            siteString = noPref[0] + "//" + siteString;
        }
        catch (ArrayIndexOutOfBoundsException ignored) {
            //
        }
        String responseString = r.getString(ConstantsFor.DBFIELD_RESPONSE) + " " + r.getString(ConstantsFor.DBFIELD_METHOD);
        siteResponseMap.putIfAbsent(siteString, responseString + " when: " + date);
    }
    
    private void makeReadableResults() {
        Set<String> keySet = siteResponseMap.keySet();
        keySet.stream().distinct().forEachOrdered(this::parseResultSetMap);
        stringBuilder.append("DENIED SITES: <br>");
        Collections.sort(toWriteAllowed);
        Collections.sort(toWriteDenied);
        Collections.reverse(toWriteDenied);
        Collections.reverse(toWriteAllowed);
        toWriteDenied.forEach(x->stringBuilder.append(x).append("<br>"));
        stringBuilder.append("<p>ALLOWED SITES: <br>");
        toWriteAllowed.forEach(x->stringBuilder.append(x).append("<br>"));
        stringBuilder.append(ConstantsFor.HTMLTAG_DETAILSCLOSE);
    }
    
    private void parseResultSetMap(String distinctKey) {
        HTMLGeneration htmlGeneration = new PageGenerationHelper();
        String valueX = siteResponseMap.get(distinctKey);
        if (!distinctKey.startsWith("http") && distinctKey.contains(":")) {
            distinctKey = distinctKey.split(":")[0];
        }
        try {
            valueX = valueX.split("when: ")[1] + ") " + valueX.split("when: ")[0];
        }
        catch (ArrayIndexOutOfBoundsException ignore) {
            //
        }
        
        if (!distinctKey.startsWith("http")) {
            distinctKey = ConstantsFor.STR_HTTPS + distinctKey;
        }
        
        if (valueX.contains("/5") | valueX.contains("/6")) {
            String errorSite = htmlGeneration.setColor("#fca503", valueX + " ||| " + htmlGeneration.getAsLink(distinctKey.trim(), distinctKey));
            toWriteAllowed.add(errorSite + " error!");
        }
        else if (valueX.contains("/4")) {
            String strDeny = htmlGeneration.setColor("red", valueX + " ||| " + htmlGeneration.getAsLink(distinctKey.trim(), distinctKey));
            toWriteDenied.add(strDeny);
        }
        else {
            String strAllow = htmlGeneration.setColor(ConstantsFor.GREEN, valueX + " ||| " + htmlGeneration.getAsLink(distinctKey.trim(), distinctKey));
            if (valueX.toLowerCase().contains("post") | valueX.toLowerCase().contains("connect") | valueX.toLowerCase().contains("tunnel")) {
                strAllow = strAllow.replace(ConstantsFor.GREEN, ConstantsFor.YELLOW);
            }
            toWriteAllowed.add(strAllow);
        }
        FileSystemWorker.writeFile("denied.log", toWriteDenied.stream().sorted());
        FileSystemWorker.writeFile("allowed.log", toWriteAllowed.stream().sorted());
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InternetUse{");
        sb.append("stringBuilder=").append(stringBuilder);
        sb.append(", siteResponseMap=").append(siteResponseMap);
        sb.append(", toWriteDenied=").append(toWriteDenied);
        sb.append(", toWriteAllowed=").append(toWriteAllowed);
        sb.append('}');
        return sb.toString();
    }
}
