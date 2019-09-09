package ru.vachok.networker.data.synchronizer;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringJoiner;


/**
 @see DBDownloaderTest
 @since 08.09.2019 (17:36) */
class DBDownloader implements SyncData {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, DBDownloader.class.getSimpleName());
    
    private int lastId = 0;
    
    private String dbToSync;
    
    private String idColName;
    
    @Contract(pure = true)
    DBDownloader(String dbToSync) {
        this.dbToSync = dbToSync;
        this.idColName = ConstantsFor.SQLCOL_IDVELKOMPC;
    }
    
    @Contract(pure = true)
    DBDownloader(@NotNull String[] dbNameColID) {
        this.dbToSync = dbNameColID[0];
        this.idColName = dbNameColID[1];
    }
    
    public String getIdColName() {
        return idColName;
    }
    
    public void setIdColName(String idColName) {
        this.idColName = idColName;
    }
    
    @Override
    public String syncData() {
        StringBuilder stringBuilder = new StringBuilder();
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.DBUSER_NETWORK);
        this.lastId = getLastID();
        stringBuilder.append(dbToSync).append(", ");
        stringBuilder.append(dataConnectTo.toString()).append(" dataConnectTo");
        
        try (Connection connection = dataConnectTo.getDataSource().getConnection()) {
            final String sql = String.format("SELECT * FROM %s", dbToSync);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery();
                     BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(dbToSync + ".table"))) {
                    while (resultSet.next()) {
                        if (resultSet.getInt(1) > lastId) {
                            for (int i = 1; i < resultSet.getMetaData().getColumnCount(); i++) {
                                bufferedOutputStream.write(resultSet.getMetaData().getColumnName(i).getBytes());
                                bufferedOutputStream.write("=".getBytes());
                                bufferedOutputStream.write(resultSet.getBytes(i));
                                bufferedOutputStream.write(",".getBytes());
                            }
                            bufferedOutputStream.write("\n".getBytes());
                            bufferedOutputStream.flush();
                        }
                    }
                }
            }
        }
        catch (SQLException | IOException e) {
            stringBuilder.append(FileSystemWorker.error(getClass().getSimpleName() + ".syncDB", e));
        }
        return stringBuilder.toString();
    }
    
    @Override
    public void setOption(Object option) {
        this.dbToSync = (String) option;
    }
    
    private int getLastID() {
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
        MysqlDataSource source = dataConnectTo.getDataSource();
        source.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        try (Connection connection = source.getConnection()) {
            final String sql = String.format("select %s from %s ORDER BY idvelkompc DESC LIMIT 1", idColName, dbToSync);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    int retInt = 0;
                    while (resultSet.next()) {
                        if (resultSet.last()) {
                            retInt = resultSet.getInt(idColName);
                        }
                    }
                    return retInt;
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 144");
            return 0;
        }
    }
    
    void writeLocalDBFromFile() {
        try (InputStream inputStream = new FileInputStream(ConstantsFor.TABLE_VELKOMPC + ".table");
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            bufferedReader.lines().forEach(x->{
                String[] arrOfLine = x.split(",");
                checkLastID(arrOfLine);
            });
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " see line: 133");
        }
    }
    
    private void checkLastID(@NotNull String[] arrOfLine) {
        this.lastId = getLastID();
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
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", DBDownloader.class.getSimpleName() + "[\n", "\n]")
            .add("lastId = " + lastId)
            .add("dbToSync = '" + dbToSync + "'")
            .add("idColName = '" + idColName + "'")
            .toString();
    }
}