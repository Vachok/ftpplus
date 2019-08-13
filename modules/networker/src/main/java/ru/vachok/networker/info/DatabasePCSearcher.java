// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.NetKeeper;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.accesscontrol.inetstats.InetUserPCName;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.internetuse.InternetUse;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToTray;

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ru.vachok.networker.restapi.DataConnectTo.messageToUser;


/**
 @see ru.vachok.networker.info.DatabasePCSearcherTest
 @since 08.08.2019 (13:20) */
public class DatabasePCSearcher implements DatabaseInfo {
    
    
    private static final Properties LOCAL_PROPS = AppComponents.getProps();
    
    private static final Pattern COMPILE = Pattern.compile(": ");
    
    public static final Set<String> PC_NAMES_SET = new TreeSet<>();
    
    /**
     Неиспользуемые имена ПК
     */
    public static Collection<String> unusedNamesTree = new TreeSet<>();
    
    public static Map<String, Boolean> netWorkMap = NetKeeper.getNetworkPCs();
    
    private static Properties PROPERTIES = AppComponents.getProps();
    
    private Connection connection;
    
    private List<String> userPCName = new ArrayList<>();
    
    private StringBuilder stringBuilder;
    
    private String aboutWhat;
    
    public DatabasePCSearcher() {
        try {
            this.connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        }
        catch (SQLException e) {
            MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
            messageToUser.error(MessageFormat.format("DatabasePCSearcher.DatabasePCSearcher: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    public @NotNull String getUserPCFromDB(@NotNull String userName) {
        StringBuilder retBuilder = new StringBuilder();
        final String sql = "select * from pcuserauto where userName like ? ORDER BY whenQueried DESC LIMIT 0, 20";
        String mostFreqName = "No Name";
        if (userName.contains(":")) {
            try {
            userName = COMPILE.split(userName)[1].trim();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            userName = userName.split(":")[1].trim();
        }
        }
        
        try (Connection c = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
             PreparedStatement p = c.prepareStatement(sql)
        ) {
            p.setString(1, "%" + userName + "%");
            try (ResultSet r = p.executeQuery()) {
                String headER = "<h3><center>LAST 20 USER (" + userName + ") PCs</center></h3>";
                this.stringBuilder = new StringBuilder();
                stringBuilder.append(headER);
                while (r.next()) {
                    rNext(r);
                }
                
                List<String> collectedNames = userPCName.stream().distinct().collect(Collectors.toList());
                Map<Integer, String> freqName = new HashMap<>();
    
                for (String dbName : collectedNames) {
                    collectFreq(dbName, freqName);
                }
                if (r.last()) {
                    rLast(r);
                }
                countCollection(collectedNames, freqName);
                return stringBuilder.toString();
            }
        }
        catch (SQLException | NoSuchElementException e) {
            retBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
        }
        return retBuilder.toString();
    }
    
    @Override
    public String getPCUsersFromDB(String pcName) {
        return pcNameInfo(pcName);
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.aboutWhat = aboutWhat;
        return theInfoFromDBGetter();
    }
    
    @Override
    public void setInfo(Object info) {
        AppComponents.netScannerSvc().setThePc((String) info);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DatabasePCSearcher{");
        sb.append('}');
        return sb.toString();
    }
    
    private @NotNull String theInfoFromDBGetter() throws UnknownFormatConversionException {
        if (new NameOrIPChecker(aboutWhat).isLocalAddress()) {
            StringBuilder sqlQBuilder = new StringBuilder();
            sqlQBuilder.append("select * from velkompc where NamePP like '%").append(aboutWhat).append("%'");
            return dbGetter(sqlQBuilder.toString());
        }
        else {
            IllegalArgumentException argumentException = new IllegalArgumentException("Must be NOT NULL!");
            return argumentException.getMessage();
        }
    }
    
    private @NotNull String dbGetter(final String sql) {
        String retStr;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            retStr = parseResultSet(resultSet);
        }
        catch (SQLException | IndexOutOfBoundsException | UnknownHostException e) {
            return MessageFormat.format("DatabasePCSearcher.dbGetter: {0}, ({1})", e.getMessage(), e.getClass().getName());
        }
        return retStr;
    }
    
    private @NotNull String parseResultSet(@NotNull ResultSet resultSet) throws SQLException, UnknownHostException {
        List<String> timeNowDatabaseFields = new ArrayList<>();
        List<Integer> integersOff = new ArrayList<>();
        while (resultSet.next()) {
            int onlineNow = resultSet.getInt(ConstantsNet.ONLINE_NOW);
            if (onlineNow == 1) {
                timeNowDatabaseFields.add(resultSet.getString(ConstantsFor.DBFIELD_TIMENOW));
            }
            else {
                integersOff.add(onlineNow);
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        String namePP = new StringBuilder()
            .append("<center><h2>").append(InetAddress.getByName(aboutWhat + ConstantsFor.DOMAIN_EATMEATRU)).append(" information.<br></h2>")
            .append("<font color = \"silver\">OnLines = ").append(timeNowDatabaseFields.size())
            .append(". Offline = ").append(integersOff.size()).append(". TOTAL: ")
            .append(integersOff.size() + timeNowDatabaseFields.size()).toString();
        stringBuilder.append(namePP).append(". <br>");
        String sortList = sortList(timeNowDatabaseFields);
        return stringBuilder.append(sortList).toString();
    }
    
    private @NotNull String sortList(List<String> timeNow) {
        Collections.sort(timeNow);
        
        String str = timeNow.get(timeNow.size() - 1);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(AppComponents.netScannerSvc().getThePc());
        stringBuilder.append("Last online: ");
        stringBuilder.append(str);
        stringBuilder.append(" (");
        stringBuilder.append(")<br>Actual on: ");
        stringBuilder.append(new Date(Long.parseLong(LOCAL_PROPS.getProperty(ConstantsNet.PR_LASTSCAN))));
        stringBuilder.append("</center></font>");
        
        String thePcWithDBInfo = stringBuilder.toString();
        AppComponents.netScannerSvc().setThePc(thePcWithDBInfo);
        setInfo(thePcWithDBInfo);
        return thePcWithDBInfo;
    }
    
    private void countCollection(List<String> collectedNames, @NotNull Map<Integer, String> freqName) {
        Collections.sort(collectedNames);
        Set<Integer> integers = freqName.keySet();
        String mostFreqName = freqName.get(Collections.max(integers));
        InternetUse internetUse = new InetUserPCName();
        stringBuilder.append("<br>");
        stringBuilder.append(internetUse.getUsage(mostFreqName));
    }
    
    private void collectFreq(String x, @NotNull Map<Integer, String> freqName) {
        int frequency = Collections.frequency(userPCName, x);
        stringBuilder.append(frequency).append(") ").append(x).append("<br>");
        freqName.putIfAbsent(frequency, x);
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
    
    private void rNext(@NotNull ResultSet r) throws SQLException {
        String pcName = r.getString(ConstantsFor.DBFIELD_PCNAME);
        userPCName.add(pcName);
        String returnER = "<br><center><a href=\"/ad?" + pcName.split("\\Q.\\E")[0] + "\">" + pcName + "</a> set: " + r
            .getString(ConstantsNet.DB_FIELD_WHENQUERIED) + ConstantsFor.HTML_CENTER_CLOSE;
        stringBuilder.append(returnER);
    }
    
    @Contract("_ -> param1")
    private String pcNameInfo(String pcName) {
        StringBuilder builder = new StringBuilder();
        this.aboutWhat = pcName;
        boolean isOnline;
        InetAddress byName;
        try {
            byName = InetAddress.getByName(pcName);
            isOnline = byName.isReachable(ConstantsFor.TIMEOUT_650);
            String someMore = getCondition(isOnline);
            if (!isOnline) {
                pcNameUnreachable(someMore, byName);
            }
            else {
                
                builder.append("<br><b><a href=\"/ad?");
                builder.append(pcName.split(".eatm")[0]);
                builder.append("\" >");
                builder.append(InetAddress.getByName(pcName));
                builder.append("</b></a>     ");
                builder.append(someMore);
                builder.append(". ");
                
                String printStr = builder.toString();
                String pcOnline = "online is true<br>";
                
                netWorkMap.put(printStr, true);
                PC_NAMES_SET.add(pcName + ":" + byName.getHostAddress() + pcOnline);
                messageToUser.info(pcName, pcOnline, someMore);
                int onlinePC = Integer.parseInt((PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC, "0")));
                onlinePC += 1;
                PROPERTIES.setProperty(PropertiesNames.PR_ONLINEPC, String.valueOf(onlinePC));
            }
        }
        catch (IOException e) {
            unusedNamesTree.add(e.getMessage());
        }
        return pcName;
    }
    
    private void pcNameUnreachable(String someMore, @NotNull InetAddress byName) {
        String onLines = new StringBuilder()
            .append("online ")
            .append(false)
            .append("<br>").toString();
        PC_NAMES_SET.add(byName.getHostName() + ":" + byName.getHostAddress() + " " + onLines);
        netWorkMap.put("<br>" + byName + " last name is " + someMore, false);
        messageToUser.warn(byName.toString(), onLines, someMore);
    }
    
    private @NotNull String getCondition(boolean isOnline) throws NoClassDefFoundError {
        StringBuilder buildEr = new StringBuilder();
        if (isOnline) {
            buildEr.append("<font color=\"yellow\">last name is ");
            InformationFactory informationFactory = new ConditionChecker("select * from velkompc where NamePP like ?");
            buildEr.append(informationFactory.getInfoAbout(aboutWhat + ":true"));
            buildEr.append("</font> ");
        }
        else {
            InformationFactory informationFactory = new ConditionChecker("select * from pcuser where pcName like ?");
            buildEr.append(informationFactory.getInfoAbout(aboutWhat + ":false"));
        }
        return buildEr.toString();
    }
}
