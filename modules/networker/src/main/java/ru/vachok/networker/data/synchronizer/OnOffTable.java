package ru.vachok.networker.data.synchronizer;


import ru.vachok.networker.componentsrepo.exceptions.TODOException;

import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;


/**
 @see OnOffTableTest
 @since 30.09.2019 (13:48) */
class OnOffTable extends SyncData {
    
    
    private static final String DB_SYNC = "velkom.onoff";
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", OnOffTable.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
    
    @Override
    String getDbToSync() {
        return this.DB_SYNC;
    }
    
    @Override
    public void setDbToSync(String dbToSync) {
        throw new UnsupportedOperationException("velkom.onoff is constant!");
    }
    
    @Override
    public void setOption(Object option) {
        throw new UnsupportedOperationException("velkom.onoff is constant!");
    }
    
    @Override
    public String syncData() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.OnOffTable.syncData( String ) at 30.09.2019 - (13:48)");
    }
    
    @Override
    public void superRun() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.OnOffTable.superRun( void ) at 30.09.2019 - (13:48)");
    }
    
    @Override
    public int uploadCollection(Collection stringsCollection, String tableName) {
        throw new TODOException("ru.vachok.networker.data.synchronizer.OnOffTable.uploadCollection( int ) at 30.09.2019 - (13:48)");
    }
    
    @Override
    Map<String, String> makeColumns() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.OnOffTable.makeColumns( Map<String, String> ) at 30.09.2019 - (13:48)");
    }
}