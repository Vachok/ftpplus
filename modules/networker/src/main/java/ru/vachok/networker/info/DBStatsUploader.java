package ru.vachok.networker.info;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.*;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringJoiner;


/**
 @see DBStatsUploaderTest
 @since 08.09.2019 (10:08) */
public class DBStatsUploader extends Stats {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, DBStatsUploader.class.getSimpleName());
    
    private DataConnectTo dataConnectTo;
    
    private String aboutWhat;
    
    @Contract(pure = true)
    public DBStatsUploader(DataConnectTo dataConnectTo) {
        this.dataConnectTo = dataConnectTo;
        this.aboutWhat = "192.168.13.220";
    }
    
    public DBStatsUploader() {
        this.dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
        this.aboutWhat = "192.168.13.220";
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.aboutWhat = aboutWhat;
        StringBuilder stringBuilder = new StringBuilder();
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.STR_VELKOM);
             PreparedStatement preparedStatement = connection.prepareStatement("select ? from " + aboutWhat)) {
            
        }
        catch (SQLException e) {
            stringBuilder.append(MessageFormat.format("DBStatsUploader.getInfo {0} - {1}", e.getClass().getTypeName(), e.getMessage()));
        }
        return stringBuilder.toString();
    }
    
    @Override
    public String getInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ConstantsFor.CONNECTING_TO).append(dataConnectTo.getDataSource().getURL()).append("\n");
        dataConnectTo.getDataSource().setDatabaseName(ConstantsFor.STR_INETSTATS);
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.STR_VELKOM)) {
            DatabaseMetaData metaData = connection.getMetaData();
            stringBuilder.append(new TForms().fromArray(connection.getTypeMap())).append("\n");
            stringBuilder.append(getCatalogs(metaData)).append("\n");
            try (PreparedStatement preparedStatement = connection.prepareStatement("select * from " + aboutWhat.replaceAll("\\Q.\\E", "_"));
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                stringBuilder.append(getMetaData(resultSet));
                while (resultSet.next()) {
                    stringBuilder.append("\"ip\" = ").append(resultSet.getString("ip")).append(" ").append(resultSet.getInt(ConstantsFor.SQLCOL_BYTES))
                        .append(resultSet.getInt("timespend")).append(" time spend").append(" ")
                        .append(ConstantsFor.STR_BYTES).append(" ").append(new Date(resultSet.getLong("stamp"))).append("\n");
                }
            }
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e));
        }
        
        return stringBuilder.toString();
    }
    
    private @NotNull String getCatalogs(DatabaseMetaData dbMetaData) {
        StringBuilder stringBuilder = new StringBuilder();
        try (ResultSet catalogs = dbMetaData.getCatalogs()) {
            while (catalogs.next()) {
                stringBuilder.append(catalogs.next()).append(" catalog").append("\n");
            }
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e));
        }
        return stringBuilder.toString();
    }
    
    private @NotNull String getMetaData(@NotNull ResultSet resultSet) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        ResultSetMetaData rsMetaData = resultSet.getMetaData();
        int countColumn = rsMetaData.getColumnCount();
        stringBuilder.append(countColumn).append(" getColumnCount, ");
        for (int i = 0; i < countColumn; i++) {
            stringBuilder.append(rsMetaData.getCatalogName(i)).append(" getCatalogName\n");
        }
        
        return stringBuilder.toString();
    }
    
    @Override
    public void setClassOption(Object option) {
        if (option instanceof DataConnectTo) {
            this.dataConnectTo = (DataConnectTo) option;
        }
        else {
            this.aboutWhat = (String) option;
        }
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", DBStatsUploader.class.getSimpleName() + "[\n", "\n]")
            .add("dataConnectTo = " + dataConnectTo)
            .toString();
    }
    
    int createUploadStatTable(String[] sql) {
        if (aboutWhat.contains(".")) {
            this.aboutWhat = aboutWhat.replaceAll("\\Q.\\E", "_");
        }
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.STR_INETSTATS)) {
            try (PreparedStatement preparedStatementCreateTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + aboutWhat + "(\n" +
                "  `idrec` mediumint(11) unsigned NOT NULL COMMENT '',\n" +
                "  `stamp` bigint(13) unsigned NOT NULL COMMENT '',\n" +
                "  `ip` varchar(20) NOT NULL COMMENT '',\n" +
                "  `bytes` int(11) NOT NULL COMMENT '',\n" +
                "  `timespend` int(11) NOT NULL DEFAULT '0',\n" +
                "  `site` varchar(254) NOT NULL COMMENT ''\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
            ) {
                if (preparedStatementCreateTable.executeUpdate() == 0) {
                    for (String sqlCom : sql) {
                        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlCom)) {
                            System.out.println("preparedStatement = " + preparedStatement.executeUpdate());
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 142 ***");
            return -10;
        }
        return 0;
    }
    
    int uploadToTable(@NotNull String[] valuesArr) {
        int retInt;
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.STR_INETSTATS)) {
            try (PreparedStatement preparedStatement = connection
                .prepareStatement("insert into " + aboutWhat.replaceAll("\\Q.\\E", "_") + "(stamp, ip, bytes, site) values (?,?,?,?)")) {
                preparedStatement.setLong(1, parseStamp(valuesArr[0]));
                preparedStatement.setString(2, valuesArr[1]);
                preparedStatement.setInt(3, Integer.parseInt(valuesArr[2]));
                preparedStatement.setString(4, valuesArr[4]);
                retInt = preparedStatement.executeUpdate();
            }
            catch (IndexOutOfBoundsException e) {
                messageToUser.error(e.getMessage() + " see line: 163 ***");
                retInt = -163;
            }
        }
        catch (SQLException e) {
            String message = e.getMessage();
            if (!message.contains("Duplicate entry")) {
                messageToUser.error(message + " see line: 168 ***");
            }
            retInt = -168;
        }
        return retInt;
    }
    
    private long parseStamp(String stringWithDate) {
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy", Locale.ENGLISH);
        Date parsedDate;
        try {
    
            parsedDate = format.parse(stringWithDate);
        }
        catch (ParseException e) {
            messageToUser.error(e.getMessage() + " see line: 176 ***");
            return 0;
        }
        return parsedDate.getTime();
    }
}