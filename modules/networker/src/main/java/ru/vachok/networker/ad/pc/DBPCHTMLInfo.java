// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLInfo;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ConstantsNet;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToTray;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.awt.*;
import java.io.File;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 @see DBPCHTMLInfoTest
 @since 18.08.2019 (17:41) */
class DBPCHTMLInfo implements HTMLInfo {
    
    
    private static final Pattern COMPILE = Pattern.compile(": ");
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, DBPCHTMLInfo.class.getSimpleName());
    
    private static final String NOT_FOUND = " not found";
    
    private String pcName;
    
    private List<String> userPCName = new ArrayList<>();
    
    private Map<Integer, String> freqName = new ConcurrentHashMap<>();
    
    private StringBuilder stringBuilder;
    
    private Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.STR_VELKOM + "." + ConstantsFor.DB_PCUSERAUTO);
    
    private String sql = ConstantsFor.SQL_GET_VELKOMPC_NAMEPP;
    
    protected String getUserNameFromNonAutoDB() {
        StringBuilder stringBuilder = new StringBuilder();
        try (PreparedStatement statementPCUser = connection.prepareStatement("select * from pcuser")) {
            stringBuilder.append(firstOnlineResultsParsing(statementPCUser));
        }
        catch (SQLException | ParseException | RuntimeException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(AbstractForms.fromArray(e));
        }
        return stringBuilder.toString();
    }
    
    private @NotNull String firstOnlineResultsParsing(@NotNull PreparedStatement statementPCUser) throws SQLException, ParseException {
        StringBuilder stringBuilder = new StringBuilder();
        try (ResultSet resultUser = statementPCUser.executeQuery()) {
            while (resultUser.next()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
                if (resultUser.getString(ConstantsFor.DBFIELD_PCNAME).toLowerCase().contains(pcName)) {
                    stringBuilder
                            .append(resultUser.getString(ConstantsFor.DBFIELD_USERNAME)).append(". Resolved: ")
                            .append(dateFormat.parse(resultUser.getString(ConstantsNet.DB_FIELD_WHENQUERIED))).append(" ");
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
            retBuilder.append(e.getMessage()).append("\n").append(AbstractForms.fromArray(e));
        }
        return retBuilder.toString();
    }
    
    DBPCHTMLInfo() {
        messageToUser.warn("SET THE PC NAME!");
    }
    
    @Contract(pure = true)
    DBPCHTMLInfo(@NotNull String pcName) {
        if (pcName.contains(ConstantsFor.EATMEAT)) {
            pcName = pcName.split("\\Q.eatmeat.ru\\E")[0];
        }
        this.pcName = pcName;
        this.connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.STR_VELKOM + "." + ConstantsFor.DB_PCUSERAUTO);
    }
    
    @Override
    public String fillWebModel() {
        String lastOn = lastOnline();
        lastOn = HTMLGeneration.getInstance("").setColor("red", lastOn);
    
        String link = new PageGenerationHelper().getAsLink("/ad?" + pcName, lastOn);
    
        return link;
    }
    
    @Override
    public String fillAttribute(String attributeName) {
        this.pcName = attributeName;
        return countOnOff();
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.pcName = (String) classOption;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DBPCInfo{");
        sb.append("pcName='").append(pcName).append('\'');
        sb.append(", sql='").append(sql).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    private @NotNull String htmlOnOffCreate(int onSize, int offSize) {
        StringBuilder stringBuilder = new StringBuilder();
        String htmlFormatOnlineTimes = MessageFormat.format(" Online = {0} times.", onSize);
        stringBuilder.append(htmlFormatOnlineTimes);
        stringBuilder.append(" Offline = ");
        stringBuilder.append(offSize);
        stringBuilder.append(" times. TOTAL: ");
        stringBuilder.append(offSize + onSize);
        return stringBuilder.toString();
    }
    
    private @NotNull String countOnOff() {
        Thread.currentThread().checkAccess();
        Thread.currentThread().setPriority(1);
        Collection<Integer> onLine = new ArrayList<>();
        Collection<Integer> offLine = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, String.format("%%%s%%", pcName));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int onlineNow = resultSet.getInt(ConstantsNet.ONLINE_NOW);
                    if (onlineNow == 1) {
                        onLine.add(1);
                    }
                    if (onlineNow == 0) {
                        offLine.add(0);
                    }
                }
            }
            catch (RuntimeException e) {
                messageToUser.error(MessageFormat.format("countOnOff: executeQuery()", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace())));
            }
        }
        catch (SQLException | RuntimeException e) {
            messageToUser.error("DBPCHTMLInfo", "countOnOff", e.getMessage() + " see line: 173");
        }
        AppComponents.threadConfig().execByThreadConfig(()->upPcUser(onLine.size(), offLine.size()));
        return htmlOnOffCreate(onLine.size(), offLine.size());
    }
    
    private void upPcUser(int on, int off) {
        final String sqlOn = "UPDATE `velkom`.`pcuser` SET `On`= " + on + " WHERE `pcName` like '" + pcName + "%'";
        final String sqlOff = "UPDATE `velkom`.`pcuser` SET `Off`= " + off + " WHERE `pcName` like '" + pcName + "%'";
        final String sqlTotal = "UPDATE `velkom`.`pcuser` SET `Total`= " + (on + off) + " WHERE `pcName` like '" + pcName + "%'";
        
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.TESTING).getDefaultConnection(ConstantsFor.DB_VELKOMPCUSER);
             PreparedStatement psOn = connection.prepareStatement(sqlOn);
             PreparedStatement psOff = connection.prepareStatement(sqlOff);
             PreparedStatement psTotal = connection.prepareStatement(sqlTotal);
        ) {
            psOn.executeUpdate();
            psOff.executeUpdate();
            psTotal.executeUpdate();
        }
        catch (SQLException e) {
            messageToUser.error("DBPCHTMLInfo.upPcUser", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace()));
        }
    }
    
    private String lastOnline() {
        @NotNull String result;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM velkom.pcuserauto WHERE pcName LIKE ? ORDER BY idRec DESC LIMIT 1")) {
            preparedStatement.setString(1, String.format("%s%%", pcName));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                String lastOnLineStr = lastOnlinePCResultsParsing(resultSet);
                if (!lastOnLineStr.contains(NOT_FOUND)) {
                    result = lastOnLineStr;
                }
                else {
                    result = searchInBigDB();
                }
            }
        }
        catch (SQLException | RuntimeException e) {
            result = MessageFormat.format("DBPCHTMLInfo.lastOnline: {0}", e.getMessage());
        }
        return result;
    }
    
    private @NotNull String lastOnlinePCResultsParsing(@NotNull ResultSet viewWhenQueriedRS) throws SQLException, NoSuchElementException {
        Deque<String> rsParsedDeque = new LinkedList<>();
        while (viewWhenQueriedRS.next()) {
            if (viewWhenQueriedRS.getString(ConstantsFor.DBFIELD_PCNAME).toLowerCase().contains(pcName.toLowerCase())) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder
                        .append(viewWhenQueriedRS.getString(ConstantsFor.DBFIELD_USERNAME)).append(" - ")
                        .append(viewWhenQueriedRS.getString(ConstantsFor.DBFIELD_PCNAME))
                        .append(". Last online: ")
                        .append(viewWhenQueriedRS.getString(ConstantsNet.DB_FIELD_WHENQUERIED));
                rsParsedDeque.addFirst(stringBuilder.toString());
            }
        }
        if (rsParsedDeque.isEmpty()) {
            return pcName + NOT_FOUND;
        }
        else {
            return rsParsedDeque.getLast();
        }
    }
    
    private String searchInBigDB() {
        String result = "Not registered in both databases...";
        final String sql = ConstantsFor.SQL_GET_VELKOMPC_NAMEPP + "AND AddressPP LIKE '%true' ORDER BY idRec DESC LIMIT 1";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, String.format("%s%%", pcName));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String userName = resultSet.getString(ConstantsFor.DBFIELD_USERNAME);
                    result = MessageFormat.format("{0} : {1}. last seen at {2}", pcName, userName, new Date(resultSet.getTimestamp(ConstantsFor.DBFIELD_TIMENOW).getTime()));
                }
            }
        }
        catch (SQLException | RuntimeException e) {
            result = MessageFormat.format("DBPCHTMLInfo.searchInBigDB: {0}", e.getMessage());
        }
        messageToUser.warn(this.getClass().getSimpleName(), sql.replace("?", String.format("%s%%", pcName)), result);
        return result;
    }
    
    private @NotNull String sortList(List<String> timeNow) {
        Collections.sort(timeNow);
        
        String str = timeNow.get(timeNow.size() - 1);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(pcName);
        stringBuilder.append("Last online: ");
        stringBuilder.append(str);
        stringBuilder.append(" (");
        stringBuilder.append(")<br>Actual on: ");
        stringBuilder.append(new Date(Long.parseLong(AppComponents.getProps().getProperty(PropertiesNames.LASTSCAN))));
        stringBuilder.append("</center></font>");
        
        return stringBuilder.toString();
    }
    
    private void writeOnOffToFile(int on, int off) {
        File onOffFile = new File("onoff.pc");
        Set<String> fileAsSet = FileSystemWorker.readFileToSet(onOffFile.toPath());
        String strToAppendOnOff = MessageFormat.format("On: {0}, off: {1}, {2}", on, off, pcName);
        fileAsSet.add(strToAppendOnOff + " ");
        FileSystemWorker.writeFile(onOffFile.getAbsolutePath(), fileAsSet.stream());
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
            messageToUser.info(r.getString(ConstantsFor.DBFIELD_PCNAME), r.getString(ConstantsNet.DB_FIELD_WHENQUERIED), r.getString(ConstantsFor.DBFIELD_USERNAME));
        }
        catch (HeadlessException e) {
            MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName())
                    .info(r.getString(ConstantsFor.DBFIELD_PCNAME), r.getString(ConstantsNet.DB_FIELD_WHENQUERIED), r.getString(ConstantsFor.DBFIELD_USERNAME));
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
        stringBuilder.append(this.getLast20UserPCs());
    }
    
}