// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.DBMessenger;

import java.io.File;
import java.util.Map;


/**
 @see ExitApp
 @since 09.06.2019 (21:10) */
public class ExitAppTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private final ExitApp exitApp = new ExitApp("test");
    
    private MessageToUser messageToUser = DBMessenger.getInstance(this.getClass().getSimpleName());
    
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
        exitApp.run();
    }
    
    @Test
    public void testWriteOwnObject() {
        boolean isWritten = new ExitApp("test.obj", FileSystemWorker.readFile("lastnetscan")).isWriteOwnObject();
        Assert.assertTrue(isWritten);
        File fileWritten = new File("test.obj");
        Assert.assertTrue(fileWritten.exists());
        fileWritten.deleteOnExit();
    }
    
    @Test
    public void testTestToString() {
        String toString = exitApp.toString();
        Assert.assertTrue(toString.contains("ExitApp{reasonExit='test'"), toString);
    }
    
    @Test
    public void testGetVisitsMap() {
        Map<Long, Visitor> visitsMap = ExitApp.getVisitsMap();
        String fromArrayStr = new TForms().fromArray(visitsMap);
        Assert.assertTrue(fromArrayStr.contains("127.0.0.1"), fromArrayStr);
    }
}