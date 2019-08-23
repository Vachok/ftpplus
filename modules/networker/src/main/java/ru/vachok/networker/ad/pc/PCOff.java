// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.net.NetKeeper;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToTray;

import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 @see ru.vachok.networker.info.PCOffTest
 @since 08.08.2019 (13:20) */
public class PCOff extends PCInfo {
    
    
    private static final Pattern COMPILE = Pattern.compile(": ");
    
    private static final String SQL = "select * from pcuser where pcName like ?";
    
    private List<String> userPCName = new ArrayList<>();
    
    private Map<Integer, String> freqName = new ConcurrentHashMap<>();
    
    private StringBuilder stringBuilder;
    
    private String pcName;
    
    private DataConnectTo dataConnectTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    public PCOff(String aboutWhat) {
        this.pcName = aboutWhat;
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.pcName = (String) classOption;
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.pcName = aboutWhat;
        return userNameFromDBWhenPCIsOff();
    }
    
    private @NotNull String userNameFromDBWhenPCIsOff() {
        if (!pcName.contains(ConstantsFor.EATMEAT)) {
            this.pcName = pcName + ConstantsFor.DOMAIN_EATMEATRU;
        }
        StringBuilder stringBuilder = new StringBuilder();
        
        try (Connection connection = dataConnectTo.getDataSource().getConnection();
             PreparedStatement p = connection.prepareStatement(SQL)) {
            p.setString(1, pcName);
            try (PreparedStatement p1 = connection.prepareStatement(SQL.replaceAll(ConstantsFor.DBFIELD_PCUSER, ConstantsFor.DBFIELD_PCUSERAUTO))) {
                p1.setString(1, "%" + pcName + "%");
                try (ResultSet resultSet = p.executeQuery()) {
                    stringBuilder.append(parseResults(resultSet, p1));
                }
            }
            
            final String sql2 = "SELECT * FROM `velkompc` WHERE `NamePP` LIKE '" + pcName + "' ORDER BY `TimeNow` DESC LIMIT 2750";
            try (PreparedStatement p2 = connection.prepareStatement(sql2);
                 ResultSet resultSet = p2.executeQuery()) {
                stringBuilder.append(findLastPCOnlineTime(resultSet));
            }
        }
        catch (SQLException | NullPointerException e) {
            stringBuilder.append("<font color=\"red\">EXCEPTION in SQL dropped. <b>");
            stringBuilder.append(e.getMessage());
            stringBuilder.append("</b></font>");
        }
        
        if (stringBuilder.toString().isEmpty()) {
            stringBuilder.append(getClass().getSimpleName()).append(" <font color=\"red\">").append(pcName).append(" null</font>");
        }
        stringBuilder.append(new DBPCInfo(pcName).lastOnline());
        return stringBuilder.toString();
    }
    
    @Override
    public String getInfo() {
        if (pcName == null) {
            return "Please - set the pcName!\n" + this.toString();
        }
        String onOffCounter = new PCOn(pcName).countOnOff();
        try {
            this.pcName = InetAddress.getByAddress(InetAddress.getByName(pcName).getAddress()).getHostName();
        }
        catch (UnknownHostException e) {
            messageToUser.error(e.getMessage());
        }
        
        return MessageFormat.format("USER: {0}, {1}", userNameFromDBWhenPCIsOff(), pcNameWithHTMLLink(onOffCounter, pcName));
    }
    
    private @NotNull String findLastPCOnlineTime(@NotNull ResultSet resultSet) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> onList = new ArrayList<>();
        while (resultSet.next()) {
            if (resultSet.getString("AddressPP").toLowerCase().contains("true")) {
                onList.add(resultSet.getString(ConstantsFor.DBFIELD_TIMENOW));
            }
        }
        Collections.sort(onList);
        Collections.reverse(onList);
        if (onList.size() > 0) {
            searchLastOnlineDate(onList, stringBuilder);
        }
        return stringBuilder.toString();
    }
    
    private void searchLastOnlineDate(@NotNull List<String> onList, StringBuilder stringBuilder) {
        String strDate = onList.get(0);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        simpleDateFormat.applyPattern("yyyy-MM-dd");
        Date dateFormat = new Date(Long.parseLong(AppComponents.getProps().getProperty(PropertiesNames.PR_LASTSCAN, String.valueOf(System.currentTimeMillis()))));
        try {
            dateFormat = simpleDateFormat.parse(strDate.split(" ")[0]);
        }
        catch (ParseException | ArrayIndexOutOfBoundsException | NumberFormatException e) {
            messageToUser.error(e.getMessage());
        }
        
        if ((dateFormat.getTime() + TimeUnit.DAYS.toMillis(5) < System.currentTimeMillis())) {
            strDate = "<font color=\"yellow\">" + strDate + "</font>";
        }
        if ((dateFormat.getTime() + TimeUnit.DAYS.toMillis(UsefulUtilities.ONE_DAY_HOURS / 2) < System.currentTimeMillis())) {
            strDate = "<font color=\"red\">" + strDate + "</font>";
            
        }
        else {
            strDate = "<font color=\"green\">" + strDate + "</font>";
        }
        stringBuilder.append("    Last online PC: ");
        stringBuilder.append(strDate);
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", PCOff.class.getSimpleName() + "[\n", "\n]")
            .add("userPCName = " + userPCName)
            .add("freqName = " + freqName)
            .add("stringBuilder = " + stringBuilder)
            .add("pcName = '" + pcName + "'")
            .add("dataConnectTo = " + dataConnectTo)
            .toString();
    }
    
    public @NotNull String pcNameWithHTMLLink(String someMore, String pcName) {
        InetAddress byName = InetAddress.getLoopbackAddress();
        try {
            byName = InetAddress.getByName(pcName);
        }
        catch (UnknownHostException e) {
            messageToUser.error(MessageFormat.format("PCOff.pcNameWithHTMLLink: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return pcNameUnreachable(someMore, byName);
    }
    
    private @NotNull String pcNameUnreachable(String onOffCounter, @NotNull InetAddress byName) {
        String onLines = new StringBuilder()
            .append("online ")
            .append(false)
            .append("<br>").toString();
        NetKeeper.getPcNamesSet().add(byName.getHostName() + ":" + byName.getHostAddress() + " " + onLines);
        NetKeeper.getNetworkPCs().put("<br>" + byName + " last name is " + onOffCounter, false);
        messageToUser.warn(byName.toString(), onLines, onOffCounter);
        return onLines + " " + onOffCounter;
    }
    
    private @NotNull String parseResults(@NotNull ResultSet resultSet, PreparedStatement p1) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        while (resultSet.next()) {
            stringBuilder.append("<b>")
                .append(resultSet.getString(ConstantsFor.DB_FIELD_USER).trim()).append("</b> (time from: <i>")
                .append(resultSet.getString(ConstantsNet.DB_FIELD_WHENQUERIED)).append("</i> to ");
        }
        if (resultSet.wasNull()) {
            stringBuilder.append("<font color=\"red\">user name is null </font>");
        }
        try (ResultSet resultSet1 = p1.executeQuery()) {
            while (resultSet1.next()) {
                if (resultSet.first()) {
                    stringBuilder.append("<i>").append(resultSet1.getString(ConstantsNet.DB_FIELD_WHENQUERIED)).append("</i>)");
                }
                if (resultSet1.last()) {
                    stringBuilder
                        .append("    (AutoResolved name: ")
                        .append(resultSet1.getString(ConstantsFor.DB_FIELD_USER).trim()).append(")").toString();
                }
                if (resultSet1.wasNull()) {
                    stringBuilder.append("<font color=\"orange\">auto resolve is null </font>");
                }
            }
        }
        return stringBuilder.toString();
    }
    
    private @NotNull String getLast20UserPCs() {
        StringBuilder retBuilder = new StringBuilder();
        final String sql = "select * from pcuserauto where userName like ? ORDER BY whenQueried DESC LIMIT 0, 20";
        if (pcName.contains(":")) {
            try {
                pcName = COMPILE.split(pcName)[1].trim();
            }
            catch (ArrayIndexOutOfBoundsException e) {
                pcName = pcName.split(":")[1].trim();
            }
        }
        
        try (Connection c = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
             PreparedStatement p = c.prepareStatement(sql)
        ) {
            p.setString(1, "%" + pcName + "%");
            try (ResultSet r = p.executeQuery()) {
                String headER = "<h3><center>LAST 20 USER (" + pcName + ") PCs</center></h3>";
                this.stringBuilder = new StringBuilder();
                stringBuilder.append(headER);
                while (r.next()) {
                    rNext(r);
                }
                
                List<String> collectedNames = userPCName.stream().distinct().collect(Collectors.toList());
                
                for (String nameFromDB : collectedNames) {
                    collectFreq(nameFromDB);
                }
                if (r.last()) {
                    rLast(r);
                }
                countCollection(collectedNames);
                return stringBuilder.toString();
            }
        }
        catch (SQLException | NoSuchElementException e) {
            retBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
        }
        return retBuilder.toString();
    }
    
    private void rNext(@NotNull ResultSet r) throws SQLException {
        String pcName = r.getString(ConstantsFor.DBFIELD_PCNAME);
        userPCName.add(pcName);
        String returnER = "<br><center><a href=\"/ad?" + pcName.split("\\Q.\\E")[0] + "\">" + pcName + "</a> set: " + r
            .getString(ConstantsNet.DB_FIELD_WHENQUERIED) + ConstantsFor.HTML_CENTER_CLOSE;
        stringBuilder.append(returnER);
    }
    
    private void collectFreq(String nameFromDB) {
        int frequency = Collections.frequency(userPCName, nameFromDB);
        stringBuilder.append(frequency).append(") ").append(nameFromDB).append("<br>");
        freqName.putIfAbsent(frequency, nameFromDB);
    }
    
    private void rLast(@NotNull ResultSet r) throws SQLException {
        try {
            ru.vachok.messenger.MessageToUser messageToUser = new MessageToTray(this.getClass().getSimpleName());
            messageToUser.info(r.getString(ConstantsFor.DBFIELD_PCNAME), r.getString(ConstantsNet.DB_FIELD_WHENQUERIED), r.getString(ConstantsFor.DB_FIELD_USER));
        }
        catch (HeadlessException e) {
            new MessageLocal(this.getClass().getSimpleName())
                .info(r.getString(ConstantsFor.DBFIELD_PCNAME), r.getString(ConstantsNet.DB_FIELD_WHENQUERIED), r.getString(ConstantsFor.DB_FIELD_USER));
        }
    }
    
    private void countCollection(List<String> collectedNames) {
        Collections.sort(collectedNames);
        Set<Integer> integers = freqName.keySet();
        String mostFreqName;
        try {
            mostFreqName = freqName.get(Collections.max(integers));
        }
        catch (RuntimeException e) {
            mostFreqName = e.getMessage();
        }
        stringBuilder.append("<br>");
        if (mostFreqName != null && !mostFreqName.isEmpty()) {
            this.pcName = mostFreqName;
        }
        stringBuilder.append(getLast20UserPCs());
    }
}
