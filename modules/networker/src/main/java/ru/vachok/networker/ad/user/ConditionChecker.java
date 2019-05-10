
// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.net.InfoWorker;
import ru.vachok.networker.net.PCUserResolver;
import ru.vachok.networker.net.enums.ConstantsNet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
class ConditionChecker implements InfoWorker {


    private static final String CLASS_NAME = ConditionChecker.class.getSimpleName();

    private static MessageToUser messageToUser = new MessageCons(ConditionChecker.class.getSimpleName());

    private static Connection connection = null;
    private boolean isOnline = false;

    private String javaID;

    private String sql;

    private String pcName;

    static {
        try{
            connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        }catch(IOException e){
            messageToUser.error(e.getMessage());
        }
    }


    public ConditionChecker(String javaID, String sql, String pcName) {
        this.javaID = javaID;
        this.sql = sql;
        this.pcName = pcName;
    }


    ConditionChecker(String sql, String pcName) {
        AppComponents.threadConfig().thrNameSet(pcName.substring(0, 6));
        this.javaID = ConstantsFor.FILE_RU_VACHOK_NETWORKER_CONSTANTS_FOR;
        this.sql = sql;
        if (pcName.contains(":")) {
            this.pcName = pcName.split(":")[0];
            this.isOnline = pcName.split(":")[1].contains("true");
        }
        else {
            this.pcName = pcName;
        }
    }

    @Override public String getInfoAbout() {
        StringBuilder stringBuilder = new StringBuilder();
        if (isOnline) {
            stringBuilder.append(getUserResolved());
            stringBuilder.append(onLinesCheck());
        }
        else {
            stringBuilder.append(offLinesCheckUser());
        }
        return stringBuilder.toString();
    }


    @Override public void setInfo() {
        throw new UnsupportedOperationException();
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConditionChecker{");
        sb.append('}');
        return sb.toString();
    }


    private String getUserResolved() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b><font color=\"white\">");
        final String sqlLoc = "SELECT * FROM `pcuser` WHERE `pcName` LIKE ?";
        try(PreparedStatement p = connection.prepareStatement(sqlLoc)){
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


    private String onLinesCheck() {
        InfoWorker pcUserResolver = new PCUserResolver(pcName);
        String classMeth = "ConditionChecker.onLinesCheck";
        Runnable rPCResolver = pcUserResolver::getInfoAbout;
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
            messageToUser.errorAlert(CLASS_NAME, "onLinesCheck", e.getMessage());
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


    private String offLinesCheckUser() {
        String methName = "offLinesCheckUser";
        StringBuilder stringBuilder = new StringBuilder();
        try (PreparedStatement p = connection.prepareStatement(sql)) {
            p.setString(1, pcName);
            try (PreparedStatement p1 = connection.prepareStatement(sql.replaceAll(ConstantsFor.DBFIELD_PCUSER, ConstantsFor.DBFIELD_PCUSERAUTO))) {
                p1.setString(1, pcName);
                try (ResultSet resultSet = p.executeQuery()) {
                    while (resultSet.next()) {
                        stringBuilder.append("<b>")
                            .append(resultSet.getString(ConstantsFor.DB_FIELD_USER).trim()).append("</b> (time from: <i>")
                            .append(resultSet.getString(ConstantsNet.DB_FIELD_WHENQUERIED)).append("</i> to ");
                    }
                    if (resultSet.wasNull()) stringBuilder.append("<font color=\"red\">user name is null </font>");
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
                            if (resultSet1.wasNull()) stringBuilder.append("<font color=\"orange\">auto resolve is null </font>");
                        }
                    }
                }
            }
            try (PreparedStatement p2 = connection.prepareStatement("SELECT * FROM `velkompc` WHERE `NamePP` LIKE '" + pcName + "' ORDER BY `TimeNow` DESC LIMIT 75");
                 ResultSet resultSet = p2.executeQuery()
            ) {
                List<String> onList = new ArrayList<>();
                while (resultSet.next()) {
                    if (resultSet.getString("AddressPP").toLowerCase().contains("true")) {
                        onList.add(resultSet.getString(ConstantsFor.DBFIELD_TIMENOW));
                    }
                }
                Collections.sort(onList);
                Collections.reverse(onList);
                if (onList.size() > 0) {
                    onSizeNotNull(onList, stringBuilder);
                }
            }
        }
        catch (SQLException | NullPointerException e) {

            messageToUser.errorAlert("ConditionChecker", methName, e.getMessage());
            stringBuilder.append("<font color=\"red\">EXCEPTION in SQL dropped. <b>");
            stringBuilder.append(e.getMessage());
            stringBuilder.append("</b></font>");
        }
        if (stringBuilder.toString().isEmpty()) stringBuilder.append(getClass().getSimpleName()).append(" <font color=\"red\">").append(methName).append(" null</font>");
        return stringBuilder.toString();
    }
    
    private void onSizeNotNull(List<String> onList, StringBuilder stringBuilder) {
        String strDate = onList.get(0);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        simpleDateFormat.applyPattern("yyyy-MM-dd");
        Date dateFormat = new Date(Long.parseLong(AppComponents.getProps().getProperty(ConstantsNet.PR_LASTSCAN)));
        try {
            dateFormat = simpleDateFormat.parse(strDate.split(" ")[0]);
        }
        catch (ParseException | ArrayIndexOutOfBoundsException e) {
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
}