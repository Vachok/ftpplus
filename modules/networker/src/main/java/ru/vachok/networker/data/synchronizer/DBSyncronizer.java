package ru.vachok.networker.data.synchronizer;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.sql.*;
import java.util.StringJoiner;


/**
 @see DBSyncronizerTest
 @since 08.09.2019 (17:36) */
class DBSyncronizer {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, DBSyncronizer.class.getSimpleName());
    
    private int lastId = getLastID();
    
    private String dbToSync;
    
    private String idColName;
    
    @Contract(pure = true)
    DBSyncronizer(String dbToSync) {
        this.dbToSync = dbToSync;
        this.idColName = ConstantsFor.SQLCOL_IDVELKOMPC;
    }
    
    public String getIdColName() {
        return idColName;
    }
    
    public void setIdColName(String idColName) {
        this.idColName = idColName;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", DBSyncronizer.class.getSimpleName() + "[\n", "\n]")
            .add("dbToSync = '" + dbToSync + "'")
            .add("idColName = '" + idColName + "'")
            .add("lastId = " + lastId)
            .toString();
    }
    
    String syncDataOverDB(String dbToSync) {
        this.dbToSync = dbToSync;
        StringBuilder stringBuilder = new StringBuilder();
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.DBUSER_NETWORK);
        int idInLocalDB = 0;
        
        stringBuilder.append(dbToSync).append(", ");
        stringBuilder.append(idInLocalDB).append(" id local db, ");
        stringBuilder.append(dataConnectTo.toString()).append(" dataConnectTo");
        
        try (Connection connection = dataConnectTo.getDataSource().getConnection()) {
            final String sql = String.format("SELECT * FROM %s", dbToSync);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (dbToSync.equalsIgnoreCase(ConstantsFor.TABLE_VELKOMPC)) {
                        idInLocalDB = getLastID();
                    }
                    while (resultSet.next()) {
                        int idvelkompc = resultSet.getInt(idColName);
                        if (idvelkompc > idInLocalDB) {
                            writeLocalOverRemote(idvelkompc, resultSet);
                        }
                    }
                    
                }
            }
        }
        catch (SQLException e) {
            stringBuilder.append(FileSystemWorker.error(getClass().getSimpleName() + ".syncDB", e));
        }
        return stringBuilder.toString();
    }
    
    private int getLastID() {
        int retInt = 20000000;
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
        MysqlDataSource source = dataConnectTo.getDataSource();
        source.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        try (Connection connection = source.getConnection()) {
            final String sql = String.format("select %s from %s ORDER BY idvelkompc DESC LIMIT 1", idColName, dbToSync);
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
    
    private void writeLocalOverRemote(int idRow, @NotNull ResultSet resultSet) {
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
        this.lastId = getLastID();
        try (Connection localConnection = dataConnectTo.getDataSource().getConnection();
             PreparedStatement psmtLocal = localConnection
                 .prepareStatement("INSERT INTO `u0466446_velkom`.`velkompc` (`idvelkompc`, `NamePP`, `AddressPP`, `SegmentPP`, `instr`, `OnlineNow`, `userName`, `TimeNow`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            psmtLocal.setInt(1, idRow);
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
    
    String writeLocalDBFromFile(String dbToSync) {
        this.dbToSync = dbToSync;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(dbToSync).append(" ");
        try (InputStream inputStream = new FileInputStream(ConstantsFor.TABLE_VELKOMPC + ".table");
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            bufferedReader.lines().forEach(x->{
                String[] arrOfLine = x.split(",");
                checkLastID(arrOfLine);
            });
        }
        catch (IOException e) {
            stringBuilder.append(e.getMessage()).append(" see line: 146 ***");
        }
        return stringBuilder.toString();
    }
    
    private void checkLastID(@NotNull String[] arrOfLine) {
        int id;
        try {
            
            id = Integer.parseInt(arrOfLine[0]);
        }
        catch (NumberFormatException e) {
            id = 0;
        }
        if (id > lastId) {
            writeToLocalDB(arrOfLine);
        }
    }
    
    private static void writeToLocalDB(@NotNull String[] arrFromStringInFileDumpedFromRemote) {
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