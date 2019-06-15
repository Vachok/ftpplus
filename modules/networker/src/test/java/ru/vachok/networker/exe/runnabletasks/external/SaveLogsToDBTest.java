// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks.external;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import java.awt.*;


/**
 @since 14.06.2019 (16:55) */
@SuppressWarnings("ALL") public class SaveLogsToDBTest {
    
    
    @Test
    public void testGetI() {
        throw new IllegalComponentStateException("15.06.2019 (17:36)");
    }
    
    @Test
    public void testStartScheduled() {
        throw new IllegalComponentStateException("15.06.2019 (17:36)");
    }
    
    @Test
    public void testShowInfo() {
        throw new IllegalComponentStateException("15.06.2019 (17:36)");
    }
    
    @Test
    public void testRun() {
        DataConnectTo dataConnectTo = new RegRuMysql();
        MysqlDataSource dataSource = dataConnectTo.getDataSource();
        final ru.vachok.stats.SaveLogsToDB LOGS_TO_DB_EXT = ru.vachok.stats.SaveLogsToDB.getI(dataSource);
        LOGS_TO_DB_EXT.startScheduled();
        
    }
}