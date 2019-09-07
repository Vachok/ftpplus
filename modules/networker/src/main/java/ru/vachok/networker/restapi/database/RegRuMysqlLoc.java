// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsNet;
import ru.vachok.networker.componentsrepo.data.enums.PropertiesNames;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.FilePropsLocal;

import java.io.*;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;


/**
 Class ru.vachok.networker.restapi.database.RegRuMysql
 <p>
 
 @see ru.vachok.networker.restapi.database.RegRuMysqlLocTest
 @since 14.07.2019 (12:16) */
class RegRuMysqlLoc implements DataConnectTo {
    
    
    private static final Properties APP_PROPS = new FilePropsLocal(ConstantsFor.class.getSimpleName()).getProps();
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, RegRuMysqlLoc.class.getSimpleName());
    
    private static final String IDVELKOMPC = "idvelkompc";
    
    private final int lastId;
    
    
    private String dbName;
    
    @Contract(pure = true)
    public RegRuMysqlLoc(String dbName) {
        this.dbName = dbName;
        this.lastId = getLastID(ConstantsFor.TABLE_VELKOMPC, IDVELKOMPC);
    }
    
    @Override
    public void setSavepoint(Connection connection) {
        throw new UnsupportedOperationException("14.07.2019 (16:17)");
    }
    
    @Override
    public MysqlDataSource getDataSource() {
        return getDataSourceLoc(dbName);
    }
    
    @Override
    public Connection getDefaultConnection(String dbName) {
        MysqlDataSource defDataSource = new MysqlDataSource();
        defDataSource.setServerName(ConstantsNet.REG_RU_SERVER);
        defDataSource.setPort(3306);
        defDataSource.setPassword(APP_PROPS.getProperty(PropertiesNames.DBPASS));
        defDataSource.setUser(APP_PROPS.getProperty(PropertiesNames.DBUSER));
        defDataSource.setEncoding("UTF-8");
        defDataSource.setCharacterEncoding("UTF-8");
        defDataSource.setDatabaseName(dbName);
        defDataSource.setUseSSL(false);
        defDataSource.setVerifyServerCertificate(false);
        defDataSource.setAutoClosePStmtStreams(true);
        defDataSource.setAutoReconnect(true);
        try {
            return defDataSource.getConnection();
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 95");
            FileSystemWorker.error(getClass().getSimpleName() + ".getDefaultConnection", e);
        }
        return DataConnectTo.getInstance(DataConnectTo.LIB_REGRU).getDefaultConnection(dbName);
    }
    
    @Override
    public Savepoint getSavepoint(Connection connection) {
        throw new UnsupportedOperationException("14.07.2019 (16:17)");
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", RegRuMysqlLoc.class.getSimpleName() + "[\n", "\n]")
                .add("dbName = '" + dbName + "'")
                .toString();
    }
    
    private @NotNull MysqlDataSource getDataSourceLoc(String dbName) {
        this.dbName = dbName;
        MysqlDataSource defDataSource = new MysqlDataSource();
        defDataSource.setServerName(ConstantsNet.REG_RU_SERVER);
        defDataSource.setPassword(APP_PROPS.getProperty(PropertiesNames.DBPASS));
        defDataSource.setUser(APP_PROPS.getProperty(PropertiesNames.DBUSER));
        defDataSource.setEncoding("UTF-8");
        defDataSource.setCharacterEncoding("UTF-8");
        defDataSource.setDatabaseName(dbName);
        defDataSource.setUseSSL(false);
        defDataSource.setVerifyServerCertificate(false);
        defDataSource.setContinueBatchOnError(true);
        defDataSource.setAutoReconnect(true);
        defDataSource.setCachePreparedStatements(true);
        try {
            defDataSource.setLoginTimeout(5);
            defDataSource.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(60));
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat
                    .format("RegRuMysqlLoc.getDataSourceLoc\n{0}: {1}\nParameters: [dbName]\nReturn: com.mysql.jdbc.jdbc2.optional.MysqlDataSource\nStack:\n{2}", e
                            .getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        return defDataSource;
    }
    
    private static int getLastID(String dbToSync, String idColName) {
        int retInt = 20000000;
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
        MysqlDataSource source = dataConnectTo.getDataSource();
        source.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        try (Connection connection = source.getConnection()) {
            String sql = "select " + idColName + " from " + dbToSync + " ORDER BY idvelkompc DESC LIMIT 1";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        if (resultSet.last()) {
                            retInt = resultSet.getInt(idColName);
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 167 ***");
        }
        return retInt;
    }
    
    void syncDB(String dbToSync) {
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.DBUSER_NETWORK);
        int idInLocalDB = 0;
        try (Connection connection = dataConnectTo.getDataSource().getConnection()) {
            final String sql = String.format("SELECT * FROM %s", dbToSync);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (dbToSync.equalsIgnoreCase(ConstantsFor.TABLE_VELKOMPC)) {
                        idInLocalDB = getLastID(dbToSync, IDVELKOMPC);
                    }
                    System.out.println("idInLocalDB = " + idInLocalDB);
                    while (resultSet.next()) {
                        int idvelkompc = resultSet.getInt(IDVELKOMPC);
                        if (idvelkompc > idInLocalDB) {
                            writeLocalOverRemote(idvelkompc, resultSet);
                        }
                    }
                    
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".syncDB", e));
        }
    }
    
    private void writeLocalOverRemote(int idvelkompc, @NotNull ResultSet resultSet) {
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
        
        try (Connection localConnection = dataConnectTo.getDataSource().getConnection();
             PreparedStatement psmtLocal = localConnection
                 .prepareStatement("INSERT INTO `u0466446_velkom`.`velkompc` (`idvelkompc`, `NamePP`, `AddressPP`, `SegmentPP`, `instr`, `OnlineNow`, `userName`, `TimeNow`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            psmtLocal.setInt(1, idvelkompc);
            psmtLocal.setString(2, resultSet.getString("NamePP"));
            psmtLocal.setString(3, resultSet.getString("AddressPP"));
            psmtLocal.setString(4, resultSet.getString("SegmentPP"));
            psmtLocal.setString(5, resultSet.getString("instr"));
            psmtLocal.setInt(6, resultSet.getInt("OnlineNow"));
            psmtLocal.setString(7, resultSet.getString("userName"));
            psmtLocal.setString(8, resultSet.getString("TimeNow"));
            psmtLocal.executeUpdate();
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 190 ***");
        }
    }
    
    void writeLocalDBFromFile(String dbToSync) {
        
        try (InputStream inputStream = new FileInputStream("velkompc.table");
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            bufferedReader.lines().forEach(x->{
                String[] arrOfLine = x.split(",");
                checkLastID(dbToSync, arrOfLine);
            });
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " see line: 201 ***");
        }
    }
    
    private void checkLastID(String dbToSync, String[] arrOfLine) {
        
        int id = 0;
        try {
            
            id = Integer.parseInt(arrOfLine[0]);
        }
        catch (NumberFormatException e) {
            id = 0;
        }
        if (id > lastId) {
            writeToLocalDB(dbToSync, arrOfLine);
        }
    }
    
    private static void writeToLocalDB(String dbToSync, @NotNull String[] arrFromStringInFileDumpedFromRemote) {
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
        try (Connection localConnection = dataConnectTo.getDataSource().getConnection();
             PreparedStatement psmtLocal = localConnection
                 .prepareStatement("INSERT INTO `u0466446_velkom`.`velkompc` (`idvelkompc`, `NamePP`, `AddressPP`, `SegmentPP`, `OnlineNow`, `userName`, `TimeNow`) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            psmtLocal.setInt(1, Integer.parseInt(arrFromStringInFileDumpedFromRemote[0]));
            psmtLocal.setString(2, arrFromStringInFileDumpedFromRemote[1]);
            psmtLocal.setString(3, arrFromStringInFileDumpedFromRemote[2]);
            psmtLocal.setString(4, arrFromStringInFileDumpedFromRemote[3]);
            
            psmtLocal.setInt(5, Integer.parseInt(arrFromStringInFileDumpedFromRemote[5]));
            psmtLocal.setString(6, arrFromStringInFileDumpedFromRemote[6]);
            psmtLocal.setString(7, arrFromStringInFileDumpedFromRemote[7]);
            psmtLocal.executeUpdate();
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 220 ***");
        }
    }
}