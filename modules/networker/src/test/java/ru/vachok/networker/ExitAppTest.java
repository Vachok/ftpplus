// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.DBMessenger;

import java.io.File;
import java.io.Serializable;
import java.text.MessageFormat;


/**
 @since 09.06.2019 (21:10) */
public class ExitAppTest implements Serializable {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new DBMessenger(this.getClass().getSimpleName());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(ExitAppTest.class.getSimpleName().substring(0, 3));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
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
            messageToUser.error(MessageFormat.format("ExitAppTest.testWriteOwnObject threw away: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        File fileWritten = new File("test");
        Assert.assertTrue(fileWritten.exists());
        fileWritten.deleteOnExit();
    }
    
}