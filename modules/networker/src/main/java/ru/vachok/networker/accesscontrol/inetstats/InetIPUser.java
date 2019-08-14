// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.info.HTMLGeneration;
import ru.vachok.networker.info.InternetUse;
import ru.vachok.networker.info.PageGenerationHelper;
import ru.vachok.networker.restapi.message.MessageLocal;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.accesscontrol.inetstats.InetIPUserTest
 @since 02.04.2019 (10:25) */
public class InetIPUser implements InternetUse {
    
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());

    private List<String> toWriteDenied = new ArrayList<>();

    private List<String> toWriteAllowed = new ArrayList<>();
    
    private Map<String, String> siteResponseMap = new ConcurrentHashMap<>();
    
    private StringBuilder stringBuilder;
    
    @Override
    public void setClassOption(Object classOption) {
        this.messageToUser = (MessageToUser) classOption;
    }
    
    @Override public String getUsage(String userCred) {
        this.stringBuilder = new StringBuilder();
        stringBuilder.append("<details><summary>Посмотреть сайты, где был юзер (BETA)</summary>");
        stringBuilder.append("Показаны только <b>уникальные</b> сайты<br>");
        stringBuilder.append(InternetUse.cleanTrash()).append(" trash rows cleaned<p>");
    
        try (Connection connection = MYSQL_DATA_SOURCE.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_DIST)) {
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
        }catch(SQLException e){
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder.toString();
    }
    
    @Override
    public void showLog() {
        messageToUser.info(toString());
    }
    
    @Override
    public String getConnectStatistics(String userCred) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(userCred).append(" : ");
        long minutesResponse = TimeUnit.MILLISECONDS.toMinutes(getStatsFromDB(userCred, SQL_RESPONSE_TIME, "inte"));
        stringBuilder.append(minutesResponse).append(" мин. (").append(String.format("%.02f", ((float) minutesResponse / (float) 60))).append(" ч.) время открытых сессий, ");
        stringBuilder.append(getStatsFromDB(userCred, SQL_BYTES, ConstantsFor.SQLCOL_BYTES) / ConstantsFor.MBYTE).append(" мегабайт трафика.");
        return stringBuilder.toString();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InetIPUser{");
        sb.append("messageToUser=").append(messageToUser.toString());
        sb.append(", toWriteDenied=").append(toWriteDenied.size());
        sb.append(", toWriteAllowed=").append(toWriteAllowed.size());
        sb.append(", siteResponseMap=").append(siteResponseMap.size());
        sb.append('}');
        return sb.toString();
    }
    
    private long getStatsFromDB(String userCred, String sql, String colLabel) {
        long result = 0;
        try (Connection connection = MYSQL_DATA_SOURCE.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userCred);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        result = result + resultSet.getLong(colLabel);
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("InetIPUser.getResponseTime: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return result;
    }
    
    private void resultSetWhileNext(@NotNull ResultSet r) throws SQLException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss z, E");
        String date = dateFormat.format(new Date(r.getLong("Date")));
        
        String siteString = r.getString("site");
        try{
            String[] noPref = siteString.split("//");
            siteString = noPref[1].split("/")[0];
            siteString = noPref[0] + "//" + siteString;
        }
        catch (ArrayIndexOutOfBoundsException ignored) {
            //
        }
        String responseString = r.getString(ConstantsFor.DBFIELD_RESPONSE) + " " + r.getString(ConstantsFor.DBFIELD_METHOD);
        siteResponseMap.putIfAbsent(siteString , responseString + " when: " + date);
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
}
