// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;


/**
 @see ru.vachok.networker.info.LocalPCInfoTest
 @since 18.08.2019 (17:41) */
public abstract class LocalPCInfo extends PCInfo {
    
    
    private static final Properties LOCAL_PROPS = AppComponents.getProps();
    
    private String aboutWhat;
    
    public static void recToDB(String pcName, String lastFileUse) {
        new LocalPCInfo.DatabaseWriter().recToDB(pcName, lastFileUse);
    }
    
    public static void recAutoDB(String user, String pc) {
        new LocalPCInfo.DatabaseWriter().recAutoDB(user, pc);
    }
    
    @Contract("_ -> new")
    public static @NotNull LocalPCInfo getInstance(String aboutWhat) {
        if (NetScanService.isReach(aboutWhat)) {
            return new PCOn(aboutWhat);
        }
        else {
            return new PCOff(aboutWhat);
        }
    }
    
    @Contract(pure = true)
    static Properties getLocalProps() {
        return LOCAL_PROPS;
    }
    
    @Contract("_ -> param1")
    public abstract @NotNull String pcNameWithHTMLLink(String someMore, String pcName);
    
    @Override
    public abstract String getInfoAbout(String aboutWhat);
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", LocalPCInfo.class.getSimpleName() + "[\n", "\n]")
                .toString();
    }
    
    /**
     Читает БД на предмет наличия юзера для <b>offline</b> компьютера.<br>
     
     @param pcName имя ПК
     @return имя юзера, время записи.
     
     @see ADSrv#getInternetUsage(String)
     */
    @NotNull String lastOnline(CharSequence pcName) {
        StringBuilder v = new StringBuilder();
        try (Connection c = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement p = c.prepareStatement("select * from pcuser")) {
                try (PreparedStatement pAuto = c
                    .prepareStatement("select * from pcuserauto where pcName in (select pcName from pcuser) order by pcName asc limit 203")) {
                    try (ResultSet resultSet = p.executeQuery()) {
                        try (ResultSet resultSetA = pAuto.executeQuery()) {
                            while (resultSet.next()) {
                                if (resultSet.getString(ConstantsFor.DBFIELD_PCNAME).toLowerCase().contains(pcName)) {
                                    v.append("<b>").append(resultSet.getString(ConstantsFor.DB_FIELD_USER)).append("</b> <br>At ")
                                        .append(resultSet.getString(ConstantsNet.DB_FIELD_WHENQUERIED));
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
    protected String getUserByPCNameFromDB(String pcName) {
        this.aboutWhat = pcName;
        return theInfoFromDBGetter();
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
            return argumentException.getMessage();
        }
    }
    
    private static class DatabaseWriter {
        
        
        private static final Pattern COMPILE = Pattern.compile(ConstantsFor.DBFIELD_PCUSER);
        
        @Override
        public String toString() {
            return new StringJoiner(",\n", LocalPCInfo.DatabaseWriter.class.getSimpleName() + "[\n", "\n]")
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
        
        private void recToDB(String userName, String pcName) {
            MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
            String sql = "insert into pcuser (pcName, userName) values(?,?)";
            String msg = userName + " on pc " + pcName + " is set.";
            try (Connection connection = new AppComponents().connection(ConstantsNet.DB_NAME);
                 PreparedStatement p = connection.prepareStatement(sql)
            ) {
                p.setString(1, userName);
                p.setString(2, pcName);
                int executeUpdate = p.executeUpdate();
                messageToUser.info(msg + " executeUpdate=" + executeUpdate);
                ConstantsNet.getPcUMap().put(pcName, msg);
            }
            catch (SQLException ignore) {
                //nah
            }
        }
    }
}