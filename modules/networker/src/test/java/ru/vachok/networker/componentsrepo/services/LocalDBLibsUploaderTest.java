package ru.vachok.networker.componentsrepo.services;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 @see LocalDBLibsUploader
 @since 16.11.2019 (20:47) */
public class LocalDBLibsUploaderTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(LocalDBLibsUploaderTest.class.getSimpleName(), System
        .nanoTime());
    
    private LocalDBLibsUploader libsUploader;
    
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
    public void initUpl() {
        Path p = Paths.get(".").normalize().toAbsolutePath();
        String libPath = p.toString() + ConstantsFor.FILESYSTEM_SEPARATOR + "lib" + ConstantsFor.FILESYSTEM_SEPARATOR + "tutu-8.0.1947.jar";
        Assert.assertTrue(new File(libPath).exists());
        this.libsUploader = new LocalDBLibsUploader("tutu", "8.0.1947", "jar", Paths.get(libPath));
    }
    
    @Test
    public void testRun() {
        libsUploader.run();
    }
    
    @Test
    public void testTestToString() {
        String toS = libsUploader.toString();
        Assert.assertEquals(toS, "LocalDBLibsUploader[\n" +
            "dataConnectTo = MySqlLocalSRVInetStat{\"tableName\":\"velkom\",\"dbName\":\"velkom\"}\n" +
            "]");
    }
}