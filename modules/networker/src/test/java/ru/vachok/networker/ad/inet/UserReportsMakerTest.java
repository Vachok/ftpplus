package ru.vachok.networker.ad.inet;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


/**
 @see UserReportsMaker
 @since 14.10.2019 (11:58) */
public class UserReportsMakerTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(UserReportsMaker.class
            .getSimpleName(), System.nanoTime());
    
    private UserReportsMaker userReportsMaker;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @BeforeMethod
    public void initReporter() {
        this.userReportsMaker = new UserReportsMaker("10.200.202.55");
    }
    
    @Test
    public void testGetInfoAbout() {
        Assert.assertTrue(delOldFiles());
    
        String makerInfoAbout = userReportsMaker.getInfoAbout("asemenov.csv");
        Assert.assertTrue(makerInfoAbout.contains("asemenov.csv"), makerInfoAbout);
    
        this.userReportsMaker = new UserReportsMaker("10.200.213.190");
        makerInfoAbout = userReportsMaker.getInfoAbout("evyrodova.csv");
        Assert.assertTrue(makerInfoAbout.contains("evyrodova.csv"), makerInfoAbout);
    
        this.userReportsMaker = new UserReportsMaker("10.200.214.53");
        makerInfoAbout = userReportsMaker.getInfoAbout("trofimenkov.csv");
        Assert.assertTrue(makerInfoAbout.contains("trofimenkov"), makerInfoAbout);
    }
    
    private boolean delOldFiles() {
        File[] files = {new File("asemenov.csv"), new File("evyrodova.csv"), new File("trofimenkov.csv")};
        boolean retBool = false;
        for (File file : files) {
            try {
                Files.deleteIfExists(file.toPath());
                retBool = !file.exists();
            }
            catch (IOException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
                retBool = file.delete();
            }
        }
        return retBool;
    }
    
    @Test
    public void testGetInfo() {
        String userReportsMakerInfo = userReportsMaker.getInfo();
        Assert.assertTrue(userReportsMakerInfo.contains("microsoft.com:443"), userReportsMakerInfo);
    }
    
    @Test
    public void testTestToString() {
        String toString = userReportsMaker.toString();
        Assert.assertEquals(toString, "UserReportsMaker{userCred='10.200.202.55'}");
    }
}