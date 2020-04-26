package ru.vachok.networker.data.synchronizer;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.info.stats.InternetSync;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 @see SyncDataTest */
public abstract class SyncData implements DataConnectTo {


    public static final String INETSYNC = "InternetSync";

    public static final String BACKUPER = "BackupDB";

    static final DataConnectTo CONNECT_TO_REGRU = DataConnectTo.getRemoteReg();

    static final DataConnectTo CONNECT_TO_LOCAL = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);

    static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, SyncData.class.getSimpleName());

    private static final String UPUNIVERSAL = "DBUploadUniversal";

    private static final String DOWNLOADER = "DBRemoteDownloader";

    private String idColName = ConstantsFor.DBCOL_IDREC;

    public abstract String getDbToSync();

    public abstract void setDbToSync(String dbToSync);

    @Override
    public MysqlDataSource getDataSource() {
        MysqlDataSource source = CONNECT_TO_LOCAL.getDataSource();
        source.setDatabaseName(FileNames.DIR_INETSTATS);
        return source;
    }

    @Override
    public abstract int uploadCollection(Collection stringsCollection, String tableName);

    @Override
    public boolean dropTable(String dbPointTable) {
        throw new TODOException("ru.vachok.networker.data.synchronizer.SyncData.dropTable( boolean ) at 20.09.2019 - (20:37)");
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Contract("_ -> new")
    @NotNull
    public static SyncData getInstance(@NotNull String type) {
        switch (type) {
            case BACKUPER:
                return new DataSynchronizer();
            case DOWNLOADER:
                return new DBRemoteDownloader(0);
            case UPUNIVERSAL:
                return new DBUploadUniversal(DataConnectTo.DBNAME_VELKOM_POINT);
            case INETSYNC:
                return new InternetSync("10.200.213.85");
            default:
                return new InternetSync(type);
        }

    }

    public abstract void setOption(Object option);

    public abstract String syncData();

    public abstract void superRun();

    public static int getLastRecId(DataConnectTo dataConnectTo, String dbID) {
        return getInstance(UPUNIVERSAL).getDBID(dataConnectTo.getDefaultConnection(dbID), dbID);
    }

    @Override
    public Connection getDefaultConnection(String dbName) {
        try {
            MysqlDataSource source = DataConnectTo.getDefaultI().getDataSource();
            source.setDatabaseName(dbName);
            return source.getConnection();
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 76 ***");
            return DataConnectTo.getDefaultI().getDefaultConnection(dbName);
        }
    }

    /**
     @param dbPointTable dbname.table
     @param additionalColumns unstandart column names <b>with type</b>
     @return {@link PreparedStatement} executeUpdate();

     @see SyncDataTest
     */
    @Override
    public abstract int createTable(String dbPointTable, List<String> additionalColumns);

    public abstract Map<String, String> makeColumns();

    int getLastLocalID(String syncDB) {
        DataConnectTo dctInst = DataConnectTo.getInstance(DataConnectTo.TESTING);
        return getDBID(dctInst.getDefaultConnection(syncDB), syncDB);
    }

    private int getDBID(@NotNull Connection connection, String syncDB) {
        int retInt = 0;
        final String sql = String.format("select %s from %s ORDER BY %s DESC LIMIT 1", getIdColName(), syncDB, getIdColName());
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    if (resultSet.last()) {
                        retInt = resultSet.getInt(getIdColName());
                    }
                }
            }
        }
        catch (SQLException e) {
            if (e.getMessage().contains("не найден")) {
                retInt = DataConnectTo.getInstance(DataConnectTo.TESTING).createTable(syncDB, Collections.EMPTY_LIST);
            }
            else {
                messageToUser.error(e.getMessage() + " see line: 169 ***");
                retInt = -666;
            }
        }
        return retInt;
    }

    @Contract(pure = true)
    private String getIdColName() {
        return idColName;
    }

    public void setIdColName(String idColName) {
        this.idColName = idColName;
    }

    int getLastRemoteID(String syncDB) {
        return getDBID(DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(syncDB), syncDB);
    }

}
