// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.MessageToUser;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.text.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 @see DBPCInfo
 @since 18.08.2019 (17:41) */
class DBPCInfo {
    
    
    private static final DataConnectTo DATA_CONNECT_TO = DataConnectTo.getDefaultI();
    
    private static final Connection DEFAULT_CONNECTION = DATA_CONNECT_TO.getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    
    private String pcName;
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    private String sql = ConstantsFor.SQL_GET_VELKOMPC_NAMEPP;
    
    @Contract(pure = true)
    DBPCInfo(@NotNull String pcName) {
        if (pcName.contains(ConstantsFor.EATMEAT)) {
            pcName = pcName.split("\\Q.eatmeat.ru\\E")[0];
        }
        this.pcName = pcName;
    }
    
    public String defaultInformation() {
        this.pcName = PCInfo.checkValidName(pcName);
        
        String onOffCount = onlineOfflineHTML();
        String lastOnline = userNameFromDBWhenPCIsOff();
        
        return MessageFormat.format("{0} - {1}", onOffCount, lastOnline);
    }
    
    private @NotNull String onlineOfflineHTML() {
        @NotNull String onOffHTML;
        if (new NameOrIPChecker(pcName).isLocalAddress()) {
            onOffHTML = dbGetLastOnlineAndOnOffHTML(sql);
        }
        else {
            throw new IllegalArgumentException(pcName);
        }
        return onOffHTML;
    }
    
    @NotNull String userNameFromDBWhenPCIsOff() {
        StringBuilder stringBuilder = new StringBuilder();
        this.sql = "select * from pcuser where pcName like ?";
        
        try (Connection connection = this.DEFAULT_CONNECTION;
             PreparedStatement p = connection.prepareStatement(sql)) {
            p.setString(1, pcName);
            try (PreparedStatement p1 = connection.prepareStatement(sql.replaceAll(ConstantsFor.DBFIELD_PCUSER, ConstantsFor.DBFIELD_PCUSERAUTO))) {
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
        stringBuilder.append(lastOnline());
        return stringBuilder.toString();
    }
    
    private @NotNull String dbGetLastOnlineAndOnOffHTML(final String sql) {
        String onOffHTML = sql;
        HTMLGeneration htmlGeneration = new PageGenerationHelper();
        try (Connection connection = DATA_CONNECT_TO.getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                final String parameterPcName = "%" + pcName + "%";
                preparedStatement.setString(1, parameterPcName);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    onOffHTML = parseResultSet(resultSet);
                }
            }
        }
        catch (IndexOutOfBoundsException | SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 91");
        }
        return htmlGeneration.setColor(ConstantsFor.COLOR_SILVER, onOffHTML);
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
    
    private @NotNull String sortList(List<String> timeNow) {
        Collections.sort(timeNow);
        
        String str = timeNow.get(timeNow.size() - 1);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(AppComponents.netScannerSvc().getThePc());
        stringBuilder.append("Last online: ");
        stringBuilder.append(str);
        stringBuilder.append(" (");
        stringBuilder.append(")<br>Actual on: ");
        stringBuilder.append(new Date(Long.parseLong(AppComponents.getProps().getProperty(PropertiesNames.PR_LASTSCAN))));
        stringBuilder.append("</center></font>");
        
        String thePcWithDBInfo = stringBuilder.toString();
        AppComponents.netScannerSvc().setClassOption(thePcWithDBInfo);
        return thePcWithDBInfo;
    }
    
    @NotNull String lastOnline() {
        StringBuilder v = new StringBuilder();
        try (Connection c = DATA_CONNECT_TO.getDataSource().getConnection()) {
            try (PreparedStatement p = c.prepareStatement("select * from pcuser")) {
                try (PreparedStatement pAuto = c
                        .prepareStatement("select * from pcuserauto where pcName in (select pcName from pcuser) order by whenQueried asc limit 203")) {
                    try (ResultSet resultSet = p.executeQuery()) {
                        try (ResultSet resultSetA = pAuto.executeQuery()) {
                            while (resultSet.next()) {
                                if (resultSet.getString(ConstantsFor.DBFIELD_PCNAME).toLowerCase().contains(pcName)) {
                                    v.append(resultSet.getString(ConstantsFor.DB_FIELD_USER)).append(" ").append(resultSet.getString(ConstantsNet.DB_FIELD_WHENQUERIED));
                                }
                            }
                            while (resultSetA.next()) {
                                if (resultSetA.getString(ConstantsFor.DBFIELD_PCNAME).toLowerCase().contains(pcName)) {
                                    v.append("<p>").append(resultSetA.getString(ConstantsFor.DB_FIELD_USER)).append(" auto QUERY at: ")
                                            .append(resultSetA.getString(ConstantsNet.DB_FIELD_WHENQUERIED));
                                }
                            }
                        }
                    }
                }
            }
            return v.toString();
        }
        catch (SQLException e) {
            v.append(e.getMessage()).append("\n").append(new TForms().fromArray(e));
            return v.toString();
        }
    }
    
    private @NotNull String parseResultSet(@NotNull ResultSet resultSet) {
        List<String> timeNowDatabaseFields = new ArrayList<>();
        List<Integer> integersOff = new ArrayList<>();
        try {
            while (resultSet.next()) {
                int onlineNow = resultSet.getInt(ConstantsNet.ONLINE_NOW);
                if (onlineNow == 1) {
                    timeNowDatabaseFields.add(resultSet.getString(ConstantsFor.DBFIELD_TIMENOW));
                }
                else {
                    integersOff.add(onlineNow);
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 113");
        }
        int onSize = timeNowDatabaseFields.size();
        int offSize = integersOff.size();
        
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(htmlOnOffCreate(onSize, offSize));
        String sortList = sortList(timeNowDatabaseFields);
        return stringBuilder.append(sortList).toString();
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
        if ((dateFormat.getTime() + TimeUnit.DAYS.toMillis(ConstantsFor.ONE_DAY_HOURS / 2) < System.currentTimeMillis())) {
            strDate = "<font color=\"red\">" + strDate + "</font>";
            
        }
        else {
            strDate = "<font color=\"green\">" + strDate + "</font>";
        }
        stringBuilder.append("    Last online PC: ");
        stringBuilder.append(strDate);
    }
    
    private @NotNull String htmlOnOffCreate(int onSize, int offSize) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append(InetAddress.getByName(pcName + ConstantsFor.DOMAIN_EATMEATRU));
        }
        catch (UnknownHostException e) {
            messageToUser.error(MessageFormat.format("DBPCInfo.htmlOnOffCreate: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        String htmlFormatOnlineTimes = MessageFormat.format("<br>Online = {0} times.", onSize);
        stringBuilder.append(htmlFormatOnlineTimes);
        stringBuilder.append(" Offline = ");
        stringBuilder.append(offSize);
        stringBuilder.append(" times. TOTAL: ");
        stringBuilder.append(offSize + onSize);
        stringBuilder.append("<br>");
        return stringBuilder.toString();
    }
    
    public void setPcName(Object classOption) {
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
    
    String resolvePCNameByUserName(String userName) {
        this.pcName = userName;
        return getLast20Info();
    }
    
    private @NotNull String getLast20Info() {
        StringBuilder stringBuilder = new StringBuilder();
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `pcuser` WHERE `userName` LIKE ? LIMIT 20")) {
                preparedStatement.setString(1, new StringBuilder().append("%").append(pcName).append("%").toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        stringBuilder.append(resultSet.getString(ConstantsFor.DBFIELD_PCNAME)).append(" : ").append(resultSet.getString(ConstantsFor.DB_FIELD_USER))
                                .append("\n");
                    }
                }
            }
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder.toString();
    }
    
    @NotNull String countOnOff() {
        
        Collection<Integer> onLine = new ArrayList<>();
        Collection<Integer> offLine = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        
        try (Connection connection = DATA_CONNECT_TO.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, String.format("%%%s%%", pcName));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int onlineNow = resultSet.getInt(ConstantsNet.ONLINE_NOW);
                    if (onlineNow == 1) {
                        onLine.add(onlineNow);
                    }
                    if (onlineNow == 0) {
                        offLine.add(onlineNow);
                    }
                }
            }
        }
        catch (SQLException | RuntimeException e) {
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder
                .append(offLine.size())
                .append(" offline times and ")
                .append(onLine.size())
                .append(" online times.").toString();
    }
}