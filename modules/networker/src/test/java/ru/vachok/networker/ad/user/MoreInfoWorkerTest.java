// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.InternetUse;
import ru.vachok.networker.accesscontrol.inetstats.InetUserPCName;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.systray.MessageToTray;
import ru.vachok.networker.systray.actions.ActionCloseMsg;

import java.awt.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 @since 10.06.2019 (16:05) */
@SuppressWarnings("ALL") public class MoreInfoWorkerTest {
    
    @Test
    public void testGetUserFromDB() {
        String userFromDB = MoreInfoWorker.getUserFromDB("user: kudr");
        Assert.assertTrue(userFromDB.contains("do0213"), userFromDB);
    }
    
    @Test
    public void testGetInfoAbout() {
        String aboutTV = new MoreInfoWorker().getInfoAbout();
        Assert.assertTrue(aboutTV.contains("ptv1.eatmeat.ru"), aboutTV);
    
        aboutTV = new MoreInfoWorker("do0213.eatmeat.ru").getInfoAbout();
        Assert.assertTrue(aboutTV.contains("ikudryashov"), aboutTV);
    }
    
    @Test
    public void testSetInfo() {
        try {
            new MoreInfoWorker().setInfo();
        }
        catch (IllegalComponentStateException e) {
            Assert.assertNotNull(e, e.getMessage());
        }
    }
    
    private static String getUserFromDB(String userInputRaw) {
        final Pattern COMPILE = Pattern.compile(": ");
        
        StringBuilder retBuilder = new StringBuilder();
        final String sql = "select * from pcuserauto where userName like ? ORDER BY whenQueried DESC LIMIT 0, 20";
        List<String> userPCName = new ArrayList<>();
        String mostFreqName = "No Name";
        
        try {
            userInputRaw = COMPILE.split(userInputRaw)[1].trim();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            userInputRaw = userInputRaw.split(":")[1].trim();
        }
        
        try (Connection c = new AppComponents().connection(ConstantsFor.DBPREFIX + ConstantsFor.STR_VELKOM);
             PreparedStatement p = c.prepareStatement(sql)
        ) {
            p.setString(1, "%" + userInputRaw + "%");
            try (ResultSet r = p.executeQuery()) {
                StringBuilder stringBuilder = new StringBuilder();
                String headER = "<h3><center>LAST 20 USER PCs</center></h3>";
                stringBuilder.append(headER);
    
                while (r.next()) {
                    rNext(r, userPCName, stringBuilder);
                }
                
                List<String> collectedNames = userPCName.stream().distinct().collect(Collectors.toList());
                Map<Integer, String> freqName = new HashMap<>();
    
                for (String x : collectedNames) {
                    collectFreq(userPCName, x, stringBuilder, freqName);
                }
                if (r.last()) {
                    rLast(r);
                }
                
                countCollection(collectedNames, mostFreqName, stringBuilder, freqName);
                return stringBuilder.toString();
            }
        }
        catch (SQLException | IOException | NoSuchElementException e) {
            retBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
        }
        return retBuilder.toString();
    }
    
    private static void countCollection(List<String> collectedNames, String mostFreqName, StringBuilder stringBuilder, Map<Integer, String> freqName) {
        Collections.sort(collectedNames);
        Set<Integer> integers = freqName.keySet();
        mostFreqName = freqName.get(Collections.max(integers));
        InternetUse internetUse = new InetUserPCName();
        stringBuilder.append("<br>");
        stringBuilder.append(internetUse.getUsage(mostFreqName));
    }
    
    private static void collectFreq(List<String> userPCName, String x, StringBuilder stringBuilder, Map<Integer, String> freqName) {
        int frequency = Collections.frequency(userPCName, x);
        stringBuilder.append(frequency).append(") ").append(x).append("<br>");
        freqName.putIfAbsent(frequency, x);
    }
    
    private static void rLast(ResultSet r) throws SQLException {
        try {
            MessageToUser messageToUser = new MessageToTray(new ActionCloseMsg(new MessageLocal("NetScanCtr")));
            messageToUser.info(r.getString(ConstantsFor.DBFIELD_PCNAME), r.getString(ConstantsNet.DB_FIELD_WHENQUERIED), r.getString(ConstantsFor.DB_FIELD_USER));
        }
        catch (UnsupportedOperationException e) {
            new MessageLocal(MoreInfoWorker.class.getSimpleName())
                .info(r.getString(ConstantsFor.DBFIELD_PCNAME), r.getString(ConstantsNet.DB_FIELD_WHENQUERIED), r.getString(ConstantsFor.DB_FIELD_USER));
        }
    }
    
    private static void rNext(ResultSet r, List<String> userPCName, StringBuilder stringBuilder) throws SQLException {
        String pcName = r.getString(ConstantsFor.DBFIELD_PCNAME);
        userPCName.add(pcName);
        String returnER = "<br><center><a href=\"/ad?" + pcName.split("\\Q.\\E")[0] + "\">" + pcName + "</a> set: " + r
            .getString(ConstantsNet.DB_FIELD_WHENQUERIED) + ConstantsFor.HTML_CENTER_CLOSE;
        stringBuilder.append(returnER);
    }
}