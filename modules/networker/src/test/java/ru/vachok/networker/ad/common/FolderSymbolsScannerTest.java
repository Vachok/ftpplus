package ru.vachok.networker.ad.common;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.nio.file.Paths;


/**
 @see FolderSymbolsScanner
 @since 10.10.2019 (13:32) */
public class FolderSymbolsScannerTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(FolderSymbolsScannerTest.class.getSimpleName(), System
            .nanoTime());

    private FolderSymbolsScanner folderSymbolsScanner = new FolderSymbolsScanner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\"));

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 3));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }

    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }

    @Test
    public void testCall() {
        try {
            String errFolders = AppConfigurationLocal.getInstance().submitAsString(folderSymbolsScanner, 30);
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }

    @Test
    public void testTestToString() {
        String string = folderSymbolsScanner.toString();
        Assert.assertTrue(string.contains("FolderSymbolsScanner{"));
    }
}