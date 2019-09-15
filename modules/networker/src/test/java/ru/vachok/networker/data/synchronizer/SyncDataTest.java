package ru.vachok.networker.data.synchronizer;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 @see SyncData
 @since 10.09.2019 (12:05) */
public class SyncDataTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(SyncData.class
            .getSimpleName(), System.nanoTime());
    
    private SyncData syncData;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        this.syncData = SyncData.getInstance("");
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @BeforeMethod
    public void initSync() {
        syncData.setIdColName("idrec");
        syncData.setDbToSync("u0466446_velkom.velkompc");
    }
    
    @Test
    public void testGetInstance() {
        String toString = syncData.toString();
        Assert.assertTrue(toString.contains("SyncInetStatistics{"), toString);
    }
    
    @Test
    public void testGetLastLocalID() {
        int lastLocalID = syncData.getLastLocalID("u0466446_velkom.velkompc");
        Assert.assertTrue(lastLocalID > 0);
    }
    
    @Test
    public void testGetLastRemoteID() {
        int lastRemoteID = syncData.getLastRemoteID("u0466446_velkom.velkompc");
        Assert.assertTrue(lastRemoteID > 0);
    }
    
    @Test
    public void getCustomIDTest() {
    
        syncData.setIdColName("counter");
        int lastRemoteID = syncData.getLastRemoteID("u0466446_webapp.ru_vachok_networker");
        Assert.assertTrue(lastRemoteID > 0, null + " lastRemoteID");
    }
    
    @Test
    public void testMakeColumns() {
        Map<String, String> map = SyncData.getInstance("").makeColumns();
        String columns = new TForms().fromArray(map);
        Assert.assertEquals(columns, "squidans : varchar(20)\n" +
            "site : varchar(190)\n" +
            "bytes : int(11)\n" +
            "stamp : bigint(13)\n");
    }
    
    @Test
    public void testGetCreateQuery() {
        Map<String, String> colMap = new HashMap<>();
        colMap.put("test", "test");
        @NotNull String[] query = SyncData.getInstance("").getCreateQuery("test.test", colMap);
        String createDB = Arrays.toString(query);
        Assert.assertEquals(createDB, "[CREATE TABLE IF NOT EXISTS test.test(\n" +
            "  `idrec` mediumint(11) unsigned NOT NULL COMMENT '',\n" +
            "  `stamp` bigint(13) unsigned NOT NULL COMMENT '',\n" +
            "  `test` test NOT NULL COMMENT '',\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n" +
            ", ALTER TABLE test.test\n" +
            "  ADD PRIMARY KEY (`idrec`);\n" +
            ", ALTER TABLE test.test\n" +
            "  MODIFY `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '';]");
    }
    
    @Test
    public void testGetDataSource() {
        MysqlDataSource source = syncData.getDataSource();
        Assert.assertEquals(source.getURL(), "jdbc:mysql://srv-inetstat.eatmeat.ru:3306/");
    }
    
    @Test
    public void testGetDefaultConnection() {
        StringBuilder stringBuilder = new StringBuilder();
        try (Connection connection = syncData.getDefaultConnection("inetstats")) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet statsTables = metaData.getTables("inetstats", "", "%", null)) {
                while (statsTables.next()) {
                    stringBuilder.append(statsTables.getString(3));
                }
            }
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Assert.assertTrue(stringBuilder.toString().contains("192_168_13_106"), stringBuilder.toString());
        Assert.assertTrue(stringBuilder.toString().contains("10_200_217_75"), stringBuilder.toString());
        Assert.assertTrue(stringBuilder.toString().contains("192_168_13_220"), stringBuilder.toString());
    }
    
    @Test
    public void testFillLimitDequeueFromDBWithFile() {
        int i = syncData.fillLimitDequeueFromDBWithFile(Paths.get(".\\inetstats\\10.10.35.30.csv"), "inetstats.10_10_35_30");
        System.out.println("i = " + i);
    }
    
    @Test
    public void testGetDbToSync() {
        throw new InvokeEmptyMethodException("GetDbToSync created 15.09.2019 at 9:12");
    }
    
    @Test
    public void testSetDbToSync() {
        throw new InvokeEmptyMethodException("SetDbToSync created 15.09.2019 at 9:12");
    }
    
    @Test
    public void testGetIdColName() {
        throw new InvokeEmptyMethodException("GetIdColName created 15.09.2019 at 9:12");
    }
    
    @Test
    public void testSetIdColName() {
        throw new InvokeEmptyMethodException("SetIdColName created 15.09.2019 at 9:12");
    }
}