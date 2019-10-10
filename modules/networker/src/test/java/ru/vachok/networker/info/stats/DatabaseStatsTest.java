package ru.vachok.networker.info.stats;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see DatabaseStats
 @since 10.10.2019 (17:27) */
public class DatabaseStatsTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private DatabaseStats databaseStats;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @BeforeMethod
    public void initStat() {
        databaseStats = new DatabaseStats();
    }
    
    @Test
    public void testGetInfoAbout() {
        try {
            String infoAboutVelkom = databaseStats.getInfoAbout("velkom");
            System.out.println("infoAboutVelkom = " + infoAboutVelkom);
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void testGetInfo() {
        String statsInfo = databaseStats.getInfo();
        Assert.assertTrue(statsInfo.contains("Start time: "), statsInfo);
        Assert.assertTrue(statsInfo.contains("Showing SLOW.LOG from MySqlLocalSRVInetStat{tableName='slow_log', dbName='mysql'}"), statsInfo);
        Assert.assertTrue(statsInfo.contains("q_time: "), statsInfo);
        Assert.assertTrue(statsInfo.contains("rows examined: "), statsInfo);
    }
    
    @Test
    public void testTestToString() {
        String string = databaseStats.toString();
        Assert.assertTrue(string.contains("DatabaseStats{"), string);
    }
}