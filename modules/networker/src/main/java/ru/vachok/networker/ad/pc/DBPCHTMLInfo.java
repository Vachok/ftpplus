// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsNet;
import ru.vachok.networker.componentsrepo.data.enums.PropertiesNames;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLInfo;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.MessageToUser;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;


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
        return new PageGenerationHelper().setColor(ConstantsFor.COLOR_SILVER, PCInfo.defaultInformation(pcName));
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.pcName = (String) classOption;
    }
    
    @NotNull String countOnOff() {
        Thread.currentThread().checkAccess();
        Thread.currentThread().setPriority(1);
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
    
    @Override
    public String fillAttribute(String attributeName) {
        this.pcName = attributeName;
        return PCInfo.defaultInformation(attributeName);
    }
    
    private @NotNull String htmlOnOffCreate(int onSize, int offSize) {
        StringBuilder stringBuilder = new StringBuilder();
        String htmlFormatOnlineTimes = MessageFormat.format(" Online = {0} times.", onSize);
        stringBuilder.append(htmlFormatOnlineTimes);
        stringBuilder.append(" Offline = ");
        stringBuilder.append(offSize);
        stringBuilder.append(" times. TOTAL: ");
        stringBuilder.append(offSize + onSize);
        stringBuilder.append("<br>");
        return stringBuilder.toString();
    }
    
    private @NotNull String lastOnlinePCResultsParsing(@NotNull ResultSet viewWhenQueriedRS) throws SQLException, NoSuchElementException {
        Deque<String> rsParsedDeque = new LinkedList<>();
        
        while (viewWhenQueriedRS.next()) {
            if (viewWhenQueriedRS.getString(ConstantsFor.DBFIELD_PCNAME).toLowerCase().contains(pcName.toLowerCase())) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder
                    .append(viewWhenQueriedRS.getString(ConstantsFor.DB_FIELD_USER)).append(" - ")
                    .append(viewWhenQueriedRS.getString(ConstantsFor.DBFIELD_PCNAME))
                    .append(". Last online: ")
                    .append(viewWhenQueriedRS.getString(ConstantsNet.DB_FIELD_WHENQUERIED));
                rsParsedDeque.addFirst(stringBuilder.toString());
            }
        }
        if (rsParsedDeque.isEmpty()) {
            return pcName + " not found";
        }
        else {
            return rsParsedDeque.getLast();
        }
    }
    
    @NotNull String lastOnline(final String sqlLoc) {
        String sqlOld = "select * from pcuserauto where pcName in (select pcName from pcuser) order by whenQueried asc limit 203";
        
        @NotNull String result;
        try (Connection connection = DATA_CONNECT_TO.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlLoc);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            String lastOnLineStr = lastOnlinePCResultsParsing(resultSet);
            if (!lastOnLineStr.isEmpty()) {
                result = lastOnLineStr;
            }
            else {
                result = lastOnline(sqlOld);
            }
        }
        catch (SQLException e) {
            result = MessageFormat.format("DBPCHTMLInfo.lastOnline: {0}, ({1})", e.getMessage(), e.getClass().getName());
        }
        return result;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DBPCInfo{");
        sb.append("pcName='").append(pcName).append('\'');
        sb.append(", sql='").append(sql).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    protected String firstOnline() {
        StringBuilder stringBuilder = new StringBuilder();
        try (Connection connection = DATA_CONNECT_TO.getDataSource().getConnection()) {
            try (PreparedStatement statementPCUser = connection.prepareStatement("select * from pcuser")) {
                stringBuilder.append(firstOnlineResultsParsing(statementPCUser));
            }
            return stringBuilder.toString();
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e));
            return stringBuilder.append(" ").toString();
        }
    }
    
    private @NotNull String firstOnlineResultsParsing(@NotNull PreparedStatement statementPCUser) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        ResultSet resultUser = statementPCUser.executeQuery();
        while (resultUser.next()) {
            if (resultUser.getString(ConstantsFor.DBFIELD_PCNAME).toLowerCase().contains(pcName)) {
                stringBuilder
                    .append(resultUser.getString(ConstantsFor.DB_FIELD_USER))
                    .append(" : ")
                    .append(ConstantsFor.DBFIELD_PCNAME).append(". Since: ")
                    .append(resultUser.getString(ConstantsNet.DB_FIELD_WHENQUERIED)).append(" ");
            }
        }
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
    
}