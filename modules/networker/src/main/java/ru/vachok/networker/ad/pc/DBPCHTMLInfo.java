// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.*;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLInfo;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.MessageToUser;

import java.sql.*;
import java.text.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 @see DBPCHTMLInfoTest
 @since 18.08.2019 (17:41) */
class DBPCHTMLInfo implements HTMLInfo {
    
    
    private static final DataConnectTo DATA_CONNECT_TO = DataConnectTo.getDefaultI();
    
    private String pcName;
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    private String sql = ConstantsFor.SQL_GET_VELKOMPC_NAMEPP;
    
    DBPCHTMLInfo() {
        messageToUser.warn("SET THE PC NAME!");
    }
    
    @Contract(pure = true)
    DBPCHTMLInfo(@NotNull String pcName) {
        if (pcName.contains(ConstantsFor.EATMEAT)) {
            pcName = pcName.split("\\Q.eatmeat.ru\\E")[0];
        }
        this.pcName = pcName;
    }
    
    @Override
    public String fillWebModel() {
        return defaultInformation();
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
    
    private @NotNull String defaultInformation() {
        this.pcName = PCInfo.checkValidName(pcName);
        String onOffCount = countOnOff();
        return MessageFormat.format("{0} - {1}", onOffCount);
    }
    
    @NotNull String countOnOff() {
        Collection<Integer> onLine = new ArrayList<>();
        Collection<Integer> offLine = new ArrayList<>();
        
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
            messageToUser.error(MessageFormat.format("DBPCHTMLInfo.countOnOff: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return htmlOnOffCreate(onLine.size(), offLine.size());
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
        return stringBuilder.append(" ").toString();
    }
    
    @NotNull String firstOnline() {
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
            return v.append(" ").toString();
        }
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
        stringBuilder.append(strDate).append(" ");
    }
    
    private @NotNull String htmlOnOffCreate(int onSize, int offSize) {
        StringBuilder stringBuilder = new StringBuilder();
        String htmlFormatOnlineTimes = MessageFormat.format("<br>Online = {0} times.", onSize);
        stringBuilder.append(htmlFormatOnlineTimes);
        stringBuilder.append(" Offline = ");
        stringBuilder.append(offSize);
        stringBuilder.append(" times. TOTAL: ");
        stringBuilder.append(offSize + onSize);
        stringBuilder.append("<br>");
        return stringBuilder.toString();
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
        stringBuilder.append(new Date(Long.parseLong(AppComponents.getProps().getProperty(PropertiesNames.PR_LASTSCAN))));
        stringBuilder.append("</center></font>");
        
        return stringBuilder.toString();
    }
}