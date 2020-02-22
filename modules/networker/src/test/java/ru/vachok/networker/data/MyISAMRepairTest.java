package ru.vachok.networker.data;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;


/**
 @see MyISAMRepair
 @since 20.11.2019 (9:11) */
public class MyISAMRepairTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(MyISAMRepairTest.class
            .getSimpleName(), System.nanoTime());

    private MyISAMRepair myISAMRepair;

    @BeforeMethod
    public void initRepair() {
        this.myISAMRepair = new MyISAMRepair();
    }

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }

    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }

    @Test
    public void testGetInfo() {
        String repairInfo = myISAMRepair.getInfo();
        String asCrashed = ConstantsFor.MARKEDASCRASHED;
        if (repairInfo.contains(asCrashed)) {
            String[] split = repairInfo.split("\n");
            for (String s : split) {
                boolean isCrashed = s.contains(asCrashed);
                Assert.assertFalse(isCrashed, s + " is crashed...");
            }
        }
    }

    @Test
    public void testGetInfoAbout() {
        String isamRepairInfoAbout = myISAMRepair.getInfoAbout(FileNames.DIR_INETSTATS);
        Assert.assertTrue(isamRepairInfoAbout.contains("SHOW TABLE STATUS FROM inetstats"), isamRepairInfoAbout);
    }

    @Test
    public void testToString() {
        String string = myISAMRepair.toString();
        Assert.assertTrue(string.contains("MyISAMRepair{"), string);
    }
}