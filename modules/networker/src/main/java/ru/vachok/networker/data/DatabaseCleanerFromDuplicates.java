package ru.vachok.networker.data;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.util.Collection;
import java.util.List;


/**
 @see DatabaseCleanerFromDuplicatesTest
 @since 25.10.2019 (21:44) */
public class DatabaseCleanerFromDuplicates implements DataConnectTo {
    
    
    @Override
    public int uploadCollection(Collection stringsCollection, String tableName) {
        throw new TODOException("ru.vachok.networker.data.DatabaseCleanerFromDuplicates.uploadCollection( int ) at 25.10.2019 - (21:44)");
    }
    
    @Override
    public boolean dropTable(String dbPointTable) {
        throw new TODOException("ru.vachok.networker.data.DatabaseCleanerFromDuplicates.dropTable( boolean ) at 25.10.2019 - (21:44)");
    }
    
    @Override
    public int createTable(String dbPointTable, List<String> additionalColumns) {
        throw new TODOException("ru.vachok.networker.data.DatabaseCleanerFromDuplicates.createTable( int ) at 04.11.2019 - (13:51)");
    }
    
    @Override
    public MysqlDataSource getDataSource() {
        throw new TODOException("ru.vachok.networker.data.DatabaseCleanerFromDuplicates.getDataSource( MysqlDataSource ) at 25.10.2019 - (21:44)");
    }
}