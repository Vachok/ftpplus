// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.Serializable;


/**
 @since 09.06.2019 (21:10) */
public class ExitAppTest implements Serializable {
    
    
    private final TestConfigure testConfigure = new TestConfigure(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigure.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigure.afterClass();
    }
    
    
    @Test(enabled = false)
    public void testRun() {
        new ExitApp("test").run();
    }
    
    @Test
    public void testWriteOwnObject() {
        boolean isWritten = new ExitApp("test", this).writeOwnObject();
        Assert.assertTrue(isWritten);
        File fileWritten = new File("test");
        Assert.assertTrue(fileWritten.exists());
        fileWritten.deleteOnExit();
    }
    
}