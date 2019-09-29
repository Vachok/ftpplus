package ru.vachok.networker.componentsrepo.fileworks;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;


public class BiggestFileInPathFinderTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(BiggestFileInPathFinderTest.class.getSimpleName(), System
        .nanoTime());
    
    private BiggestFileInPathFinder biggestFileInPathFinder;
    
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
    public void initNewClass() {
        this.biggestFileInPathFinder = new BiggestFileInPathFinder(Paths.get("d:\\TotalCMD\\Programm\\"));
    }
    
    @Test
    public void testPackFiles() {
        try {
            String packStr = biggestFileInPathFinder.packFiles(Collections.singletonList(new File("test.test")), "test.zip");
            System.out.println("packStr = " + packStr);
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testFindBiggestFile() {
        String fileStr = biggestFileInPathFinder.findBiggestFile();
        Assert.assertTrue(fileStr.contains("d:\\TotalCMD\\Programm\\"));
    }
    
    @Test
    public void testTestToString() {
        String s = biggestFileInPathFinder.toString();
        Assert.assertEquals(s, "BiggestFileInPathFinder[\n" +
            "inThePath = d:\\TotalCMD\\Programm\n" +
            "]");
    }
}