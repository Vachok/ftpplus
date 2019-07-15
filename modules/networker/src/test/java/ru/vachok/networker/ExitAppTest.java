// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.io.Serializable;


/**
 @since 09.06.2019 (21:10) */
public class ExitAppTest implements Serializable {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(ExitAppTest.class.getSimpleName().substring(0, 3));
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
    }
    
    
    @Test(enabled = false)
    public void testRun() {
        new ExitApp("test").run();
    }
    
    @Test
    public void testWriteOwnObject() {
        boolean isWritten = new ExitApp("test", this).writeOwnObject();
        try {
            Assert.assertTrue(isWritten);
        }
        catch (AssertionError e) {
            testConfigureThreadsLogMaker.getPrintStream().println(e.getMessage() + "\n" + new TForms().fromArray(e.getStackTrace(), false));
        }
        File fileWritten = new File("test");
        Assert.assertTrue(fileWritten.exists());
        fileWritten.deleteOnExit();
    }
    
}