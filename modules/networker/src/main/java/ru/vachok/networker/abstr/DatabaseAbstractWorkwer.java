package ru.vachok.networker.abstr;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;


/**
 @since 10.04.2019 (8:56) */
public class DatabaseAbstractWorkwer implements DataBaseRegSQL {
    
    
    private static final String DB_INSERT = "insert into";
    
    private static final String DB_SELECT = "select";
    
    private static final String DB_UPDATE = "update";
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private String schemaName;
    
    private String[] columnName;
    
    public DatabaseAbstractWorkwer(String schemaName, String[] columnName) {
        this.schemaName = schemaName;
        this.columnName = columnName;
    }
    
    @Override public void setSavepoint(Connection connection) {
        try {
            connection.setAutoCommit(false);
            Savepoint savepoint = connection.setSavepoint();
            messageToUser.warn(getClass().getSimpleName() + ".setSavepoint", "savepoint Id ", " = " + savepoint.getSavepointId());
        }
        catch (SQLException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".setSavepoint", e));
        }
    }
    
    @Override public MysqlDataSource getDataSource() {
        MysqlDataSource retSource = new RegRuMysql().getDataSource();
        retSource.setDatabaseName(schemaName);
        retSource.setRequireSSL(false);
        retSource.setRelaxAutoCommit(true);
        retSource.setUseSSL(false);
        retSource.setEncoding("UTF-8");
        retSource.setContinueBatchOnError(true);
        retSource.setAutoReconnect(true);
        return retSource;
    }
    
    @Override public Savepoint getSavepoint(Connection connection) {
        throw new UnsupportedOperationException();
    }
    
    @Override public int selectFrom() {
        try (Connection connection = getDataSource().getConnection()) {
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override public int insertTo() {
        return 0;
    }
    
    @Override public int deleteFrom() {
        return 0;
    }
    
    @Override public int updateTable() {
        return 0;
    }
}
