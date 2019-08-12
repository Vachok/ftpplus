// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;


/**
 @see ru.vachok.networker.info.DatabasePCSearcherTest
 @since 08.08.2019 (13:20) */
public class DatabasePCSearcher implements InformationFactory {
    
    private static final Properties LOCAL_PROPS = AppComponents.getProps();
    
    private Connection connection;
    
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
}
