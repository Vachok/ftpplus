// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 Проверки из классов.
 <p>
 Пинги, и тп
 
 @since 31.01.2019 (0:20) */
class ConditionChecker implements InformationFactory {
    
    
    private static final String FILE_RU_VACHOK_NETWORKER_CONSTANTS_FOR = "ru_vachok_networker-ConstantsFor";
    
    private static final String CLASS_NAME = ConditionChecker.class.getSimpleName();
    
    private static MessageToUser messageToUser = new MessageLocal(ConditionChecker.class.getSimpleName());
    
    private static Connection connection;
    
    private boolean isOnline;
    
    private String javaID;
    
    private String sql;
    
    private String pcName;
    
    @Contract(pure = true)
    public ConditionChecker(String javaID, String sql, String pcName) {
        this.javaID = javaID;
        this.sql = sql;
        this.pcName = pcName;
    }
    
    @Contract(pure = true)
    public ConditionChecker(String sql) {
        this.javaID = FILE_RU_VACHOK_NETWORKER_CONSTANTS_FOR;
        this.sql = sql;
    }
    
    static {
        try {
            connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("ConditionChecker.static initializer: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.pcName = checkString(aboutWhat);
        ThreadConfig.thrNameSet(pcName.substring(0, 6));
        
        StringBuilder stringBuilder = new StringBuilder();
        if (isOnline) {
            stringBuilder.append(getUserResolved());
            stringBuilder.append(countOnOff());
        }
        else {
            stringBuilder.append(userNameFromDBWhenPCIsOff());
        }
        return stringBuilder.toString();
    }
    
    @Override
    public void setClassOption(Object classOption) {
        throw new InvokeEmptyMethodException("08.08.2019 (12:48)");
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConditionChecker{");
        sb.append('}');
        return sb.toString();
    }
    
    @Contract("_ -> param1")
    private String checkString(@NotNull String aboutWhat) {
        if (aboutWhat.contains(":")) {
            this.pcName = aboutWhat.split(":")[0];
            this.isOnline = aboutWhat.split(":")[1].contains("true");
            return pcName;
        }
        else {
            return aboutWhat;
        }
    }
    
    private @NotNull String getUserResolved() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b><font color=\"white\">");
        final String sqlLoc = "SELECT * FROM `pcuser` WHERE `pcName` LIKE ?";
        try (PreparedStatement p = connection.prepareStatement(sqlLoc)) {
            p.setString(1, pcName);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    stringBuilder.append(r.getString(ConstantsFor.DB_FIELD_USER));
                }
            }
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage());
        }
        stringBuilder.append("</b></font> ");
        return stringBuilder.toString();
    }
    
    private @NotNull String countOnOff() {
        InformationFactory userResolver = new AppComponents().getUserResolver();
        Runnable rPCResolver = ()->userResolver.getInfoAbout(pcName);
        Collection<Integer> onLine = new ArrayList<>();
        Collection<Integer> offLine = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        
        Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(rPCResolver);
        
        try (
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, pcName);
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
        catch (SQLException e) {
            messageToUser.errorAlert(CLASS_NAME, "countOnOff", e.getMessage());
            stringBuilder.append(e.getMessage());
        }
        catch (NullPointerException e) {
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder
            .append(offLine.size())
            .append(" offline times and ")
            .append(onLine.size())
            .append(" online times.").toString();
    }
    
    private @NotNull String userNameFromDBWhenPCIsOff() {
        if (!pcName.contains(ConstantsFor.EATMEAT)) {
            this.pcName = pcName + ConstantsFor.DOMAIN_EATMEATRU;
        }
        StringBuilder stringBuilder = new StringBuilder();
        try (PreparedStatement p = connection.prepareStatement(sql)) {
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
    
    private void searchLastOnlineDate(List<String> onList, StringBuilder stringBuilder) {
        String strDate = onList.get(0);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        simpleDateFormat.applyPattern("yyyy-MM-dd");
        Date dateFormat = new Date(Long.parseLong(AppComponents.getProps().getProperty(ConstantsNet.PR_LASTSCAN, String.valueOf(System.currentTimeMillis()))));
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
}