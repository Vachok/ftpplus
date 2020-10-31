package ru.vachok.networker.data.synchronizer;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Deque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 @see SyncData
 @since 10.09.2019 (12:05) */
public class SyncDataTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(SyncData.class
        .getSimpleName(), System.nanoTime());

    private final String dbToSync = ConstantsFor.DB_VELKOMVELKOMPC;

    private SyncData syncData;

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        this.syncData = SyncData.getInstance("10.200.213.85");
    }

    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }

    @BeforeMethod
    public void initSync() {
        syncData.setIdColName(ConstantsFor.DBCOL_IDREC);
        syncData.setDbToSync(dbToSync);

    }

    @Test
    public void testGetInstance() {
        String toString = syncData.toString();
        Assert.assertEquals(toString, "InternetSync{ipAddr='velkom.velkompc', dbFullName='inetstats.10_200_213_85', connection=\n" +
            "}");
    }

    @Test
    @Ignore
    public void testGetLastLocalID() {
        int lastLocalID = syncData.getLastLocalID(dbToSync);
        if (UsefulUtilities.thisPC().toLowerCase().contains("home")) {
            Assert.assertTrue(lastLocalID > 0, dbToSync);
        }
    }

    @Test
    public void testGetLastRemoteID() {
        int lastRemoteID = syncData.getLastRemoteID(dbToSync);
        Assert.assertTrue(lastRemoteID > 0);
    }

    @Test
    public void getCustomIDTest() {
        syncData.setIdColName("counter");
        int lastRemoteID = syncData.getLastRemoteID("test.test");
        Assert.assertTrue(lastRemoteID == 13, null + " lastRemoteID");
    }

    @Test
    public void testMakeColumns() {
        try {
            Map<String, String> map = SyncData.getInstance("").makeColumns();
            Assert.fail();
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
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
        String sData;
        try {
            sData = syncData.syncData();
            Assert.assertTrue(sData.contains("velkom.velkompc-0.txt"), sData);
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        syncData.setDbToSync("test.test");
        try {
            sData = syncData.syncData();
            Assert.assertTrue(sData.contains("test.test-0.txt"), sData);
        }
        catch (IllegalArgumentException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }

    @Test
    public void testSuperRun() {
        syncData.superRun();
    }

    @Test
    public void testUploadCollection() {
        this.syncData = new DBUploadUniversal(FileNames.BUILD_GRADLE);
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
        Assert.assertNotNull(stringDeque);
        String jsonStr = new TForms().fromArray(stringDeque);
        Assert.assertFalse(jsonStr.isEmpty(), jsonStr);
    }
}