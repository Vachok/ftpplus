package ru.vachok.networker.data.synchronizer;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 @see SyncData
 @since 10.09.2019 (12:05) */
public class SyncDataTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(SyncData.class
        .getSimpleName(), System.nanoTime());
    
    private final String dbToSync = ConstantsFor.DBBASENAME_U0466446_VELKOM + "." + ConstantsFor.TABLE_VELKOMPC;
    
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
        syncData.setDbToSync(dbToSync);
        
    }
    
    @Test
    public void testGetInstance() {
        String toString = syncData.toString();
        Assert.assertTrue(toString.contains("SyncInetStatistics{"), toString);
    }
    
    @Test
    public void testGetLastLocalID() {
        int lastLocalID = syncData.getLastLocalID(dbToSync);
        Assert.assertTrue(lastLocalID > 0);
    }
    
    @Test(invocationCount = 5)
    public void testGetLastRemoteID() {
        int lastRemoteID = syncData.getLastRemoteID(dbToSync);
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
        Assert.assertEquals(columns, "squidans : VARCHAR(20) NOT NULL DEFAULT 'no data'\n" +
            "site : VARCHAR(190) NOT NULL DEFAULT 'no data'\n" +
            "bytes : int(11)\n" +
            "stamp : bigint(13)\n");
    }
    
    @Test
    public void testGetCreateQuery() {
        Map<String, String> colMap = new HashMap<>();
        colMap.put("test", "varchar(4) NOT NULL DEFAULT 'test',");
        @NotNull String[] query = SyncData.getInstance("").getCreateQuery("test.test", colMap);
        String createDB = Arrays.toString(query);
        Assert.assertEquals(createDB, "[CREATE TABLE IF NOT EXISTS test.test(\n" +
            "  `idrec` INT(11),\n" +
            "  `stamp` BIGINT(13) NOT NULL DEFAULT '442278000000' ,\n" +
            "  `test` varchar(4) NOT NULL DEFAULT 'test',) ENGINE=InnoDB DEFAULT CHARSET=utf8;\n" +
            ", ALTER TABLE test.test\n" +
            "  ADD PRIMARY KEY (`idrec`);\n" +
            ", ALTER TABLE test.test\n" +
            "  MODIFY `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '';]");
    }
    
    @Test
    public void testGetDataSource() {
        MysqlDataSource source = syncData.getDataSource();
        Assert.assertEquals(source.getURL(), "jdbc:mysql://srv-inetstat.eatmeat.ru:3306/inetstats");
    }
    
    @Test
    public void testGetDefaultConnection() {
        StringBuilder stringBuilder = new StringBuilder();
        try (Connection connection = syncData.getDefaultConnection(FileNames.DIR_INETSTATS)) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet statsTables = metaData.getTables(FileNames.DIR_INETSTATS, "", "%", null)) {
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
        String file = getClass().getResource("/10.10.35.30.csv").getFile();
        System.out.println("file = " + file);
        int i = syncData.fillLimitDequeueFromDBWithFile(Paths.get(new File(file).getAbsolutePath()), "inetstats.10_10_35_30");
        Assert.assertTrue(i > 0);
    }
    
    @Test
    public void testDropTable() {
        try {
            syncData.dropTable("test.test");
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testSyncData() {
        String sData = getClass().getSimpleName();
        try {
            sData = syncData.syncData();
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        syncData.setDbToSync("test.test");
        try {
            sData = syncData.syncData();
        }
        catch (IllegalArgumentException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testSuperRun() {
        try {
            syncData.superRun();
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testUploadCollection() {
        try {
            int i = syncData.uploadCollection(FileSystemWorker.readFileToList(FileNames.BUILD_GRADLE), "test.test");
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetFromFileToJSON() {
        Queue<String> stringsQ = FileSystemWorker.readFileToQueue(Paths.get(FileNames.BUILD_GRADLE));
        Deque<String> stringDeque = new ConcurrentLinkedDeque<>(stringsQ);
        syncData.setFromFileToJSON(stringDeque);
        Assert.assertNotNull(stringDeque);
        String jsonStr = new TForms().fromArray(stringDeque);
        Assert.assertFalse(jsonStr.isEmpty(), jsonStr);
    }
}