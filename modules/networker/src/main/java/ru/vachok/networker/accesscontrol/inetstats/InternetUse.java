// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.*;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.statistics.Stats;
import ru.vachok.networker.statistics.WeeklyInternetStats;

import java.sql.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.*;


/**
 @see ru.vachok.networker.accesscontrol.inetstats.InternetUseTest
 @since 02.04.2019 (10:24) */
public abstract class InternetUse implements Callable<Object>, Stats {
    
    
    public static final MysqlDataSource MYSQL_DATA_SOURCE = new RegRuMysql().getDataSourceSchema(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    
    private static MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.DB, InternetUse.class.getSimpleName());
    
    private int cleanedRows;
    
    private StringBuilder stringBuilder;
    
    private Map<String, String> siteResponseMap = new ConcurrentHashMap<>();
    
    private List<String> toWriteDenied = new ArrayList<>();
    
    private List<String> toWriteAllowed = new ArrayList<>();
    
    @Contract(" -> new")
    public static @NotNull Stats getInetUse() {
        if (Stats.isSunday()) {
            return new WeeklyInternetStats();
        }
        else {
            return new AccessLog();
        }
    }
    
    @Override
    public Object call() {
        return cleanTrash();
    }
    
    @Override
    public abstract String getInfoAbout(String aboutWhat);
    
    @Override
    public abstract void setClassOption(@NotNull Object classOption);
    
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
    
    @Override
    public String writeLog(String logName, String information) {
        return FileSystemWorker.writeFile(logName, information);
    }
    
    @Override
    public abstract String getInfo();
    
    /**
     @return куда ходил юзер
 
     @throws RejectedExecutionException {@link InternetUse#scheduleCleanDatabase()} при попытке запланированного запуска в тестах
     @param ipAddr <b>IP адрес</b>
     */
    @NotNull String getHTMLUsage(String ipAddr) {
        AppComponents.threadConfig().execByThreadConfig(this::cleanTrash);
        this.stringBuilder = new StringBuilder();
        ipAddr = new NameOrIPChecker(ipAddr).resolveIP().getHostAddress();
        stringBuilder.append("<details><summary>Посмотреть сайты (BETA)</summary>");
        stringBuilder.append("Показаны только <b>уникальные</b> сайты<br>");
        stringBuilder.append(cleanedRows).append(" trash rows cleaned<p>");
        
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(ConstantsFor.SQL_SELECT_DIST)) {
                preparedStatement.setString(1, ipAddr);
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
    
    private void resultSetWhileNext(@NotNull ResultSet r) throws SQLException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss z, E");
        String date = dateFormat.format(new Date(r.getLong("Date")));
        
        String siteString = r.getString("site");
        try {
            String[] splittedSiteNoHTTP = siteString.split("//");
            siteString = splittedSiteNoHTTP[1].split("/")[0];
            siteString = splittedSiteNoHTTP[0] + "//" + siteString;
        }
        catch (ArrayIndexOutOfBoundsException ignored) {
            //
        }
        String responseString = r.getString(ConstantsFor.DBFIELD_RESPONSE) + " " + r.getString(ConstantsFor.DBFIELD_METHOD);
        siteResponseMap.putIfAbsent(siteString,
                MessageFormat.format("{0} when: {1} ({2} bytes, {3} seconds)", responseString, date, r.getInt(ConstantsFor.SQLCOL_BYTES), r.getInt("inte")));
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
    
    private int cleanTrash() {
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
        scheduleCleanDatabase();
        this.cleanedRows = retInt;
        scheduleCleanDatabase();
        return retInt;
    }
    
    @Contract(pure = true)
    private void scheduleCleanDatabase() throws RejectedExecutionException {
        ScheduledThreadPoolExecutor taskScheduler = AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor();
        Runnable cleanRun = this::cleanTrash;
        for (Runnable runnable : taskScheduler.getQueue()) {
            if (cleanRun.equals(runnable)) {
                taskScheduler.remove(runnable);
            }
        }
        taskScheduler.schedule(cleanRun, ConstantsFor.DELAY * 2, TimeUnit.MINUTES);
    }
}
