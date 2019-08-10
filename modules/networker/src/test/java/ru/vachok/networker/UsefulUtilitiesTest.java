// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.net.scanner.NetListsTest;


/**
 @see UsefulUtilities
 @since 10.08.2019 (11:40) */
public class UsefulUtilitiesTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
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
    public void testGetMailRules() {
        throw new TODOException("10.08.2019 (11:51)");
    }
    
    @Test
    public void testIsPingOK() {
        throw new TODOException("10.08.2019 (11:51)");
    }
    
    @Test
    public void testGetStringsVisit() {
        throw new TODOException("10.08.2019 (11:51)");
    }
    
    @Test
    public void testThisPC() {
        throw new TODOException("10.08.2019 (11:50)");
    }
    
    @Test
    public void testGetVis() {
        throw new TODOException("10.08.2019 (11:50)");
    }
    
    @Test
    public void testGetMyTime() {
        throw new TODOException("10.08.2019 (11:50)");
    }
    
    @Test
    public void testGetDelay() {
        throw new TODOException("10.08.2019 (11:50)");
    }
    
    @Test
    public void testStartTelnet() {
        throw new TODOException("10.08.2019 (11:50)");
    }
    
    @Test
    public void testGetUpTime() {
        throw new TODOException("10.08.2019 (11:50)");
    }
    
    @Test
    public void testGetAtomicTime() {
        throw new TODOException("10.08.2019 (11:50)");
    }
    
    @Test
    public void testGetHTMLCenterRed() {
        String testColor = UsefulUtilities.getHTMLCenterColor("test", "red");
        Assert.assertTrue(testColor.contains("color=\"red\""), testColor);
        testColor = UsefulUtilities.getHTMLCenterColor("test", ConstantsFor.GREEN);
        Assert.assertTrue(testColor.contains("color=\"green\""), testColor);
    }
}