// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.*;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Date;
import java.util.*;


/**
 @see DBPCInfo
 @since 18.08.2019 (17:41) */
class DBPCInfo extends PCInfo {
    
    private String aboutWhat;
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    @Contract(pure = true)
    DBPCInfo(String type) {
        this.aboutWhat = type;
    }
    
    public String getPCbyUser(String userName) {
        this.aboutWhat = userName;
        return getLast20Info();
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.aboutWhat = (String) classOption;
    }
    
    @Override
    public String getInfo() {
        try {
            this.aboutWhat = InetAddress.getByAddress(InetAddress.getByName(this.aboutWhat).getAddress()).getHostName();
        }
        catch (UnknownHostException e) {
            messageToUser.error(e.getMessage());
        }
        String infoAbout = getInfoAbout(aboutWhat);
        String last20 = new PCOff(aboutWhat).getInfo();
        String retStr = MessageFormat.format("{0}\n{1}", infoAbout, last20);
        return retStr;
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.aboutWhat = aboutWhat;
        return getUserByPC();
    }
    
    private @NotNull String getUserByPC() {
        return theInfoFromDBGetter();
    }
    
    private @NotNull String dbGetter(final String sql) {
        String retStr;
        HTMLGeneration htmlGeneration = new PageGenerationHelper();
        DataConnectTo dataConnectTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        
        try (Connection connection = dataConnectTo.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            retStr = parseResultSet(resultSet);
        }
        catch (SQLException | IndexOutOfBoundsException | UnknownHostException e) {
            return MessageFormat.format("DatabasePCSearcher.dbGetter: {0}, ({1})", e.getMessage(), e.getClass().getName());
        }
        return htmlGeneration.setColor(ConstantsFor.COLOR_SILVER, retStr);
    }
    
    private @NotNull String theInfoFromDBGetter() throws UnknownFormatConversionException {
        if (aboutWhat.contains(ConstantsFor.EATMEAT)) {
            aboutWhat = aboutWhat.split("\\Q.eatmeat.ru\\E")[0];
        }
        if (new NameOrIPChecker(aboutWhat).isLocalAddress()) {
            StringBuilder sqlQBuilder = new StringBuilder();
            sqlQBuilder.append("select * from velkompc where NamePP like '%").append(aboutWhat).append("%'");
            return dbGetter(sqlQBuilder.toString());
        }
        else {
            IllegalArgumentException argumentException = new IllegalArgumentException("Must be NOT NULL!");
            return argumentException.getMessage() + " " + this.toString();
        }
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
        setClassOption(thePcWithDBInfo);
        return thePcWithDBInfo;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", DBPCInfo.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
    
    @NotNull String lastOnline() {
        StringBuilder v = new StringBuilder();
        try (Connection c = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement p = c.prepareStatement("select * from pcuser")) {
                try (PreparedStatement pAuto = c
                    .prepareStatement("select * from pcuserauto where pcName in (select pcName from pcuser) order by pcName asc limit 203")) {
                    try (ResultSet resultSet = p.executeQuery()) {
                        try (ResultSet resultSetA = pAuto.executeQuery()) {
                            while (resultSet.next()) {
                                if (resultSet.getString(ConstantsFor.DBFIELD_PCNAME).toLowerCase().contains(aboutWhat)) {
                                    v.append("<b>").append(resultSet.getString(ConstantsFor.DB_FIELD_USER)).append("</b> <br>At ")
                                        .append(resultSet.getString(ConstantsNet.DB_FIELD_WHENQUERIED));
                                }
                            }
                            while (resultSetA.next()) {
                                if (resultSetA.getString(ConstantsFor.DBFIELD_PCNAME).toLowerCase().contains(aboutWhat)) {
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
    
    private @NotNull String getLast20Info() {
        StringBuilder stringBuilder = new StringBuilder();
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `pcuser` WHERE `userName` LIKE ? LIMIT 20")) {
                preparedStatement.setString(1, new StringBuilder().append("%").append(aboutWhat).append("%").toString());
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
        
        stringBuilder.append(InetAddress.getByName(aboutWhat + ConstantsFor.DOMAIN_EATMEATRU))
            .append(MessageFormat.format("<br>Online = {0} times.", timeNowDatabaseFields.size())).append(" Offline = ").append(integersOff.size())
            .append(" times. TOTAL: ")
            .append(integersOff.size() + timeNowDatabaseFields.size()).append("<br>");
        
        String sortList = sortList(timeNowDatabaseFields);
        return stringBuilder.append(sortList).toString();
    }
    
    
}