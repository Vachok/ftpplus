// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.*;
import ru.vachok.networker.accesscontrol.inetstats.InternetUse;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.net.NetKeeper;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;


/**
 @see ru.vachok.networker.info.PCInfoTest
 @since 13.08.2019 (17:15) */
public abstract class PCInfo implements InformationFactory {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PCInfo.class.getSimpleName());
    
    protected static final Properties LOCAL_PROPS = AppComponents.getProps();
    
    public String getUserByPC(String pcName) {
        InformationFactory userName = new PCOff(pcName);
        String infoAbout = userName.getInfoAbout(pcName);
        if (infoAbout.contains("</b>")) {
            infoAbout = infoAbout.split("</b>")[0].replace("<b>", "");
        }
        return infoAbout;
    }
    
    public String getPCbyUser(String userName) {
        InformationFactory byPCName = new PCOn(userName);
        return byPCName.getInfoAbout(userName);
    }
    
    public long getStatsFromDB(String userCred, String sql, String colLabel) throws UnknownHostException {
        long result = 0;
        InetAddress address = InetAddress.getByName(userCred);
        userCred = address.getHostAddress();
        try (Connection connection = InternetUse.MYSQL_DATA_SOURCE.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userCred);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        result = result + resultSet.getLong(colLabel);
                    }
                    return result;
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("DatabaseInfo.getStatsFromDB: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return -1;
        }
    }
    
    @Contract("_ -> new")
    public static @NotNull PCInfo getInstance(String aboutWhat) {
        if (NetScanService.isReach(aboutWhat)) {
            return new PCOn(aboutWhat);
        }
        else {
            return new PCOff(aboutWhat);
        }
    }
    
    static void recToDB(String pcName, String lastFileUse) {
        new PCInfo.DatabaseWriter().recToDB(pcName, lastFileUse);
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", PCInfo.class.getSimpleName() + "[\n", "\n]")
                .toString();
    }
    
    @Override
    public abstract String getInfoAbout(String aboutWhat);
    
    @Override
    public abstract void setClassOption(Object classOption);
    
    public abstract String getInfo();
    
    static void recAutoDB(String user, String pc) {
        new PCInfo.DatabaseWriter().recAutoDB(user, pc);
    }
    
    @Contract("_ -> new")
    static @NotNull PCInfo getLocalInfo(String type) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(type);
        }
        catch (UnknownHostException e) {
            inetAddress = byBytes(type);
        }
        if (inetAddress.equals(InetAddress.getLoopbackAddress())) {
            return new DBPCInfo(type);
        }
        else {
            throw new TODOException("21.08.2019 (11:56)");
        }
    }
    
    private static InetAddress byBytes(String type) {
        try {
            return InetAddress.getByAddress(InetAddress.getByName(type).getAddress());
        }
        catch (UnknownHostException e) {
            return InetAddress.getLoopbackAddress();
        }
    }
    
    static class DatabaseWriter {
        
        
        private static final Pattern COMPILE = Pattern.compile(ConstantsFor.DBFIELD_PCUSER);
    
        private static final ru.vachok.networker.restapi.MessageToUser messageToUser = MessageToUser
                .getInstance(MessageToUser.LOCAL_CONSOLE, PCInfo.DatabaseWriter.class.getSimpleName());
        
        @Override
        public String toString() {
            return new StringJoiner(",\n", PCInfo.DatabaseWriter.class.getSimpleName() + "[\n", "\n]")
                    .toString();
        }
        
        private static void recAutoDB(String pcName, String lastFileUse) {
            DataConnectTo dataConnectTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
            Properties properties = AppComponents.getProps();
            final String sql = "insert into pcuser (pcName, userName, lastmod, stamp) values(?,?,?,?)";
            MysqlDataSource dSource = dataConnectTo.getDataSource();
            dSource.setUser(properties.getProperty(PropertiesNames.PR_DBUSER));
            dSource.setPassword(properties.getProperty(PropertiesNames.PR_DBPASS));
            dSource.setAutoReconnect(true);
            dSource.setUseSSL(false);
            
            try (Connection connection = dSource.getConnection()) {
                final String sqlReplaced = COMPILE.matcher(sql).replaceAll(ConstantsFor.DBFIELD_PCUSERAUTO);
                try (PreparedStatement preparedStatement = connection.prepareStatement(sqlReplaced)) {
                    String[] split = lastFileUse.split(" ");
                    preparedStatement.setString(1, pcName);
                    preparedStatement.setString(2, split[0]);
                    preparedStatement.setString(3, UsefulUtilities.thisPC());
                    preparedStatement.setString(4, split[7]);
                    System.out.println(preparedStatement.executeUpdate() + " " + sql);
                }
                catch (SQLException e) {
                
                }
            }
            catch (SQLException | ArrayIndexOutOfBoundsException | NullPointerException e) {
            
            }
        }
    
        private void recToDB(String pcName, String userName) {
            String sql = "insert into pcuser (pcName, userName) values(?,?)";
            String msg = userName + " on pc " + pcName + " is set.";
            DataConnectTo dataConnectTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
            
            try (Connection connection = dataConnectTo.getDataSource().getConnection();
                 PreparedStatement p = connection.prepareStatement(sql)) {
                p.setString(1, userName);
                p.setString(2, pcName);
                int executeUpdate = p.executeUpdate();
                messageToUser.info(msg + " executeUpdate=" + executeUpdate);
                
                NetKeeper.getPcUser().put(pcName, msg);
            }
            catch (SQLException ignore) {
                //nah
            }
        }
        
        private String writeDB() throws SQLException {
            int exUpInt = 0;
            List<String> list = new ArrayList<>();
            DataConnectTo dataConnectTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
            try (Connection connection = dataConnectTo.getDataSource().getConnection();
                 PreparedStatement p = connection.prepareStatement("insert into  velkompc (NamePP, AddressPP, SegmentPP , OnlineNow) values (?,?,?,?)")) {
                List<String> toSort = new ArrayList<>(NetKeeper.getPcNamesSet());
                toSort.sort(null);
                for (String x : toSort) {
                    String pcSegment = "Я не знаю...";
                    if (x.contains("200.200")) {
                        pcSegment = "Торговый дом";
                    }
                    if (x.contains("200.201")) {
                        pcSegment = "IP телефоны";
                    }
                    if (x.contains("200.202")) {
                        pcSegment = "Техслужба";
                    }
                    if (x.contains("200.203")) {
                        pcSegment = "СКУД";
                    }
                    if (x.contains("200.204")) {
                        pcSegment = "Упаковка";
                    }
                    if (x.contains("200.205")) {
                        pcSegment = "МХВ";
                    }
                    if (x.contains("200.206")) {
                        pcSegment = "Здание склада 5";
                    }
                    if (x.contains("200.207")) {
                        pcSegment = "Сырокопоть";
                    }
                    if (x.contains("200.208")) {
                        pcSegment = "Участок убоя";
                    }
                    if (x.contains("200.209")) {
                        pcSegment = "Да ладно?";
                    }
                    if (x.contains("200.210")) {
                        pcSegment = "Мастера колб";
                    }
                    if (x.contains("200.212")) {
                        pcSegment = "Мастера деликатесов";
                    }
                    if (x.contains("200.213")) {
                        pcSegment = "2й этаж. АДМ.";
                    }
                    if (x.contains("200.214")) {
                        pcSegment = "WiFiCorp";
                    }
                    if (x.contains("200.215")) {
                        pcSegment = "WiFiFree";
                    }
                    if (x.contains("200.217")) {
                        pcSegment = "1й этаж АДМ";
                    }
                    if (x.contains("200.218")) {
                        pcSegment = "ОКК";
                    }
                    if (x.contains("192.168")) {
                        pcSegment = "Может быть в разных местах...";
                    }
                    if (x.contains("172.16.200")) {
                        pcSegment = "Open VPN авторизация - сертификат";
                    }
                    boolean onLine = false;
                    if (x.contains("true")) {
                        onLine = true;
                    }
                    String x1 = x.split(":")[0];
                    p.setString(1, x1);
                    String x2 = x.split(":")[1];
                    p.setString(2, x2.split("<")[0]);
                    p.setString(3, pcSegment);
                    p.setBoolean(4, onLine);
                    exUpInt += p.executeUpdate();
                    list.add(x1 + " " + x2 + " " + pcSegment + " " + onLine);
                }
            }
            messageToUser.warn(PCInfo.DatabaseWriter.class.getSimpleName() + ".writeDB", "executeUpdate: ", " = " + exUpInt);
            return new TForms().fromArray(list, true);
        }
    }
}
