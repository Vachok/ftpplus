package ru.vachok.networker.accesscontrol.inetstats;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.fileworks.ProgrammFilesWriter;
import ru.vachok.networker.fileworks.WriteFilesTo;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.stats.SaveLogsToDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;


/**
 @since 02.04.2019 (10:25) */
public class InetIPUser implements InternetUse {


    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());

    private List<String> toWriteDenied = new ArrayList<>();

    private List<String> toWriteAllowed = new ArrayList<>();

    private ProgrammFilesWriter programmFilesWriter = new WriteFilesTo(getClass().getSimpleName());

    @Override public String getUsage(String userCred) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<details><summary>Посмотреть сайты, где был юзер (BETA)</summary>");
        Map<String, String> siteResponseMap = new HashMap<>();
        stringBuilder.append("Показаны только <b>уникальные</b> сайты<br>");
        stringBuilder.append(cleanDBClients1()).append("<p>");
        try (Connection c = MYSQL_DATA_SOURCE.getConnection()) {
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, userCred);
                try (ResultSet r = p.executeQuery()) {
                    while (r.next()) {
                        resultSetWhileNext(siteResponseMap , r);
                    }
                    if(r.last()) {
                        LocalDate localDate = LocalDateTime.ofEpochSecond((r.getLong("Date") / 1000) , 0 , ZoneOffset.ofHours(3)).toLocalDate();
                        int compareTo = LocalDate.now().compareTo(localDate);
                        stringBuilder.append("<b>Stats for ").append(compareTo).append(" days</b><br>");
                    }
                    if(r.wasNull()) stringBuilder.append("No usage detected");
                    makeReadableResults(siteResponseMap , stringBuilder);
                }
            }
        }catch(SQLException e){
            messageToUser.error(programmFilesWriter.error(getClass().getSimpleName() + ".getUsage" , e));
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder.toString();
    }


    private String cleanDBClients1() {
        StringBuilder stringBuilder = new StringBuilder();
        final String sql = "DELETE  FROM `inetstats` WHERE `site` LIKE '%clients1.google%'";
        try(Connection c = MYSQL_DATA_SOURCE.getConnection();
            PreparedStatement p = c.prepareStatement(sql)
        )
        {
            stringBuilder.append(p.executeUpdate()).append(" rows in statement: ").append(sql);
        }catch(SQLException e){
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder.toString();
    }

    @Override public void showLog() {
        SaveLogsToDB saveLogsToDB = new AppComponents().saveLogsToDB();
        saveLogsToDB.showInfo();
    }


    private void resultSetWhileNext(Map<String, String> siteResponseMap , ResultSet r) throws SQLException {
        Date date = new Date(r.getLong("Date"));
        String siteString = r.getString("site");
        try{
            String[] noPref = siteString.split("//");
            siteString = noPref[1].split("/")[0];
            siteString = noPref[0] + "//" + siteString;
        }catch(ArrayIndexOutOfBoundsException e){
            messageToUser.error(programmFilesWriter.error(getClass().getSimpleName() + ".getUsage" , e));
        }
        String responseString = r.getString("response") + " " + r.getString("method");
        siteResponseMap.putIfAbsent(siteString , responseString + " when: " + date);
    }


    private void makeReadableResults(Map<String, String> siteResponseMap, StringBuilder stringBuilder) {
        Set<String> keySet = siteResponseMap.keySet();

        keySet.stream().distinct().forEachOrdered(distinctKey -> parseResultSetMap(distinctKey , siteResponseMap , stringBuilder));
        stringBuilder.append("DENIED SITES: <br>");
        Collections.sort(toWriteAllowed);
        Collections.sort(toWriteDenied);
        Collections.reverse(toWriteDenied);
        Collections.reverse(toWriteAllowed);
        toWriteDenied.forEach(x->stringBuilder.append(x).append("<br>"));
        stringBuilder.append("<p>ALLOWED SITES: <br>");
        toWriteAllowed.forEach(x->stringBuilder.append(x).append("<br>"));
        stringBuilder.append("</details>");
    }


    private void parseResultSetMap(String x , Map<String, String> siteResponseMap , StringBuilder stringBuilder) {

        String valueX = siteResponseMap.get(x);
        try {
            valueX = valueX.split("when: ")[1] + ") " + valueX.split("when: ")[0];
        }
        catch (ArrayIndexOutOfBoundsException ignore) {
            //
        }
        if (valueX.contains("/403")) {
            String strDeny = "<font color=\"red\">" + valueX + " ||| " + "<a href=\"" + x.trim() + "\">" + x + "</a></font>";
            toWriteDenied.add(strDeny);
        }
        else {
            String strAllow = "<font color=\"green\">" + valueX + " ||| " + "<a href=\"" + x.trim() + "\">" + x + "</a></font>";
            if (valueX.toLowerCase().contains("post") | valueX.toLowerCase().contains("connect")) strAllow = strAllow.replace("green", "yellow");
            toWriteAllowed.add(strAllow);
        }
        FileSystemWorker.writeFile("denied.log", toWriteDenied.stream().sorted());
        FileSystemWorker.writeFile("allowed.log", toWriteAllowed.stream().sorted());
    }
}
