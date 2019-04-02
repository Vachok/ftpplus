package ru.vachok.networker.accesscontrol.inetstats;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

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
    
    @Override public String getUsage(String userCred) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<details><summary>Посмотреть сайты, где был юзер (BETA)</summary>");
        Map<String, String> siteResponseMap = new HashMap<>();
        
        try (Connection c = MYSQL_DATA_SOURCE.getConnection()) {
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, userCred);
                try (ResultSet r = p.executeQuery()) {
                    if (r.first()) {
                        LocalDate localDate = LocalDateTime.ofEpochSecond((r.getLong("Date") / 1000), 0, ZoneOffset.ofHours(3)).toLocalDate();
                        int compareTo = LocalDate.now().compareTo(localDate);
                        stringBuilder.append("<b>Stats for " + compareTo + " days</b><br>");
                    }
                    while (r.next()) {
                        Date date = new Date(r.getLong("Date"));
                        String siteString = r.getString("site");
                        try {
                            String[] noPref = siteString.split("//");
                            siteString = noPref[1].split("/")[0];
                            siteString = noPref[0] + "//" + siteString;
                        }
                        catch (ArrayIndexOutOfBoundsException e) {
                            siteString = siteString;
                        }
                        String responseString = r.getString("response") + " " + r.getString("method");
                        siteResponseMap.putIfAbsent(siteString, responseString + " when: " + date);
                    }
                    if (r.wasNull()) stringBuilder.append("No usage detected");
                    makeReadableResults(siteResponseMap, stringBuilder);
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".getUsage", e));
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder.toString();
    }
    
    private void makeReadableResults(Map<String, String> siteResponseMap, StringBuilder stringBuilder) {
        Set<String> keySet = siteResponseMap.keySet();
        
        keySet.stream().distinct().forEachOrdered(distinctKey->parseResultSet(distinctKey, siteResponseMap, stringBuilder));
        stringBuilder.append("DENIED SITES: <br>");
        Collections.sort(toWriteAllowed);
        Collections.sort(toWriteDenied);
        toWriteDenied.forEach(x->stringBuilder.append(x).append("<br>"));
        stringBuilder.append("<p>ALLOWED SITES: <br>");
        toWriteAllowed.forEach(x->stringBuilder.append(x).append("<br>"));
        stringBuilder.append("</details>");
    }
    
    private void parseResultSet(String x, Map<String, String> siteResponseMap, StringBuilder stringBuilder) {
        
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