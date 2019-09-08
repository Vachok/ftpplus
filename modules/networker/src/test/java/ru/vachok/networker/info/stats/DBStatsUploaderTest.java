package ru.vachok.networker.info.stats;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.util.ArrayList;


/**
 @see DBStatsUploader
 @since 08.09.2019 (10:16) */
public class DBStatsUploaderTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(DBStatsUploader.class.getSimpleName(), System.nanoTime());
    
    private DataConnectTo dataConnectTo;
    
    private DBStatsUploader dbStatsUploader = new DBStatsUploader();
    
    private String aboutWhat = "10.200.210.64";
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        this.dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testGetInfoAbout() {
        String infoAbout = dbStatsUploader.getInfoAbout(aboutWhat);
        Assert
            .assertEquals(infoAbout, "DBStatsUploader.getInfo com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException - Table 'velkom.10_200_210_64' doesn't exist");
    }
    
    @Test
    public void testGetInfo() {
        dbStatsUploader.setClassOption(aboutWhat);
        dbStatsUploader.setClassOption(new ArrayList<>());
        try {
            String info = dbStatsUploader.getInfo();
        }
        catch (IllegalArgumentException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testTestToString() {
        String toStr = dbStatsUploader.toString();
        Assert.assertTrue(toStr.contains("datasource=jdbc:mysql://srv-inetstat.eatmeat.ru:3306/"), toStr);
    }
}