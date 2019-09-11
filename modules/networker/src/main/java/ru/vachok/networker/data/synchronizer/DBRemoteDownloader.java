package ru.vachok.networker.data.synchronizer;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.io.*;
import java.sql.*;


/**
 @see DBRemoteDownloaderTest
 @since 08.09.2019 (17:36) */
class DBRemoteDownloader extends SyncData {
    
    
    private int lastLocalId;
    
    DBRemoteDownloader(int lastLocalID) {
        this.lastLocalId = lastLocalID;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DBRemoteDownloader{");
        sb.append("lastId=").append(lastLocalId);
        sb.append(", idColName='").append(getIdColName()).append('\'');
        sb.append(", dbToSync='").append(getDbToSync()).append('\'');
        sb.append(", get from db='").append(CONNECT_TO_REGRU.getDataSource().getURL()).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public String syncData() {
        StringBuilder stringBuilder = new StringBuilder();
    
        this.lastLocalId = getLastLocalID();
    
        stringBuilder.append(getDbToSync()).append(", ");
        stringBuilder.append(CONNECT_TO_REGRU.toString()).append(" dataConnectTo");
        stringBuilder.append(sqlConnect());
        
        return stringBuilder.toString();
    }
    
    @Override
    public void setOption(Object option) {
        this.setDbToSync((String) option);
    }
    
    String writeJSON() {
        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(getDbToSync() + FileNames.EXT_TABLE))) {
            bufferedOutputStream.write(sqlConnect().getBytes());
            bufferedOutputStream.flush();
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " see line: 161");
        }
        return getDbToSync() + FileNames.EXT_TABLE;
    }
    
    private @NotNull String makeJSONString(@NotNull ResultSet resultSet) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\n \"");
        stringBuilder.append(getDbToSync());
        stringBuilder.append("\" :[\n");
        if (resultSet.first()) {
            stringBuilder.append("{");
        }
        while (resultSet.next()) {
            if (resultSet.getInt(1) > lastLocalId) {
                for (int i = 1; i < resultSet.getMetaData().getColumnCount(); i++) {
                    stringBuilder.append("\"");
                    stringBuilder.append(resultSet.getMetaData().getColumnName(i));
                    stringBuilder.append("\":\"");
                    stringBuilder.append(resultSet.getString(i));
                    stringBuilder.append("\",");
                }
                stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "");
                stringBuilder.append("},\n{");
            }
        }
        if (resultSet.last()) {
            int length = stringBuilder.length();
            stringBuilder.replace((length - 4), length, "}\n]\n}");
        }
        return stringBuilder.toString();
    }
    
    private @NotNull String sqlConnect() {
        StringBuilder stringBuilder = new StringBuilder();
        try (Connection connection = CONNECT_TO_REGRU.getDataSource().getConnection()) {
            final String sql = String.format("SELECT * FROM %s WHERE idrec > %s", getDbToSync(), getLastLocalID());
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    String jsonStr = makeJSONString(resultSet);
                    stringBuilder.append(jsonStr);
                }
            }
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
        }
        return stringBuilder.toString();
    }
    
    private void checkLastID(@NotNull String[] arrOfLine) {
        this.lastLocalId = getLastLocalID();
        int id;
        try {
            id = Integer.parseInt(arrOfLine[0]);
        }
        catch (NumberFormatException e) {
            id = 0;
        }
        if (id > lastLocalId) {
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