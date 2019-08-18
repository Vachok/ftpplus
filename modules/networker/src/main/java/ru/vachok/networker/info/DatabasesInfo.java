// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.fileworks.FileSystemWorker;
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

import static ru.vachok.networker.restapi.DataConnectTo.messageToUser;


/**
 Class ru.vachok.networker.info.DatabasesInfo
 <p>
 
 @since 18.08.2019 (17:41) */
public abstract class DatabasesInfo extends PCInfo {
    
    
    private static String aboutWhat = "Credentials not set";
    
    @Contract(pure = true)
    public static String getAboutWhat() {
        return aboutWhat;
    }
    
    public static void setAboutWhat(String aboutWhat) {
        DatabasesInfo.aboutWhat = aboutWhat;
    }
    
    public static void recToDB(String pcName, String lastFileUse) {
        new DatabasesInfo.DatabaseWriter().recToDB(pcName, lastFileUse);
    }
    
    public static void recAutoDB(String user, String pc) {
        new DatabasesInfo.DatabaseWriter().recAutoDB(user, pc);
    }
    
    @Contract("_ -> new")
    public static @NotNull DatabasesInfo getI(String pc) {
        DatabasesInfo.setAboutWhat(pc);
        return new PCInDBSearcher();
    }
    
    @Override
    public String getInfo() {
        return null;
    }
    
    /**
     Читает БД на предмет наличия юзера для <b>offline</b> компьютера.<br>
     
     @param pcName имя ПК
     @return имя юзера, время записи.
     
     @see ADSrv#getInternetUsage(String)
     */
    private static @NotNull String offNowGetU(CharSequence pcName) {
        StringBuilder v = new StringBuilder();
        try (Connection c = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement p = c.prepareStatement("select * from pcuser");
                 PreparedStatement pAuto = c.prepareStatement("select * from pcuserauto where pcName in (select pcName from pcuser) order by pcName asc limit 203");
                 ResultSet resultSet = p.executeQuery();
                 ResultSet resultSetA = pAuto.executeQuery()
            ) {
                while (resultSet.next()) {
                    if (resultSet.getString(ConstantsFor.DBFIELD_PCNAME).toLowerCase().contains(pcName)) {
                        v.append("<b>").append(resultSet.getString(ConstantsFor.DB_FIELD_USER)).append("</b> <br>At ")
                            .append(resultSet.getString(ConstantsNet.DB_FIELD_WHENQUERIED));
                    }
                }
                while (resultSetA.next()) {
                    if (resultSetA.getString(ConstantsFor.DBFIELD_PCNAME).toLowerCase().contains(pcName)) {
                        v.append("<p>").append(resultSet.getString(ConstantsFor.DB_FIELD_USER)).append(" auto QUERY at: ")
                            .append(resultSet.getString(ConstantsNet.DB_FIELD_WHENQUERIED));
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(FileSystemWorker.error(ADSrv.class.getSimpleName() + ".offNowGetU", e));
        }
        return v.toString();
    }
    
    private @NotNull String theInfoFromDBGetter() throws UnknownFormatConversionException {
        if (aboutWhat.contains(ConstantsFor.EATMEAT)) {
            this.aboutWhat = aboutWhat.split("\\Q.eatmeat.ru\\E")[0];
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
        stringBuilder.append(new Date(Long.parseLong(AppComponents.getProps().getProperty(ConstantsNet.PR_LASTSCAN))));
        stringBuilder.append("</center></font>");
        
        String thePcWithDBInfo = stringBuilder.toString();
        AppComponents.netScannerSvc().setThePc(thePcWithDBInfo);
        setClassOption(thePcWithDBInfo);
        return thePcWithDBInfo;
    }
    
    @Override
    protected abstract String getUserByPCNameFromDB(String pcName);
    
    private static class DatabaseWriter {
        
        
        private static final Pattern COMPILE = Pattern.compile(ConstantsFor.DBFIELD_PCUSER);
        
        @Override
        public String toString() {
            return new StringJoiner(",\n", DatabasesInfo.DatabaseWriter.class.getSimpleName() + "[\n", "\n]")
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