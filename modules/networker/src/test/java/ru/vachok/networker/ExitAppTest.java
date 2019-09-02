// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.jetbrains.annotations.Contract;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.DBMessenger;

import java.io.*;
import java.nio.file.Files;
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
    
    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        ExitAppTest test = (ExitAppTest) o;
        
        if (!testConfigureThreadsLogMaker.equals(test.testConfigureThreadsLogMaker)) {
            return false;
        }
        if (!exitApp.equals(test.exitApp)) {
            return false;
        }
        return messageToUser.equals(test.messageToUser);
    }
    
    private void readExt(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            ExitApp app = new ExitApp();
            app.readExternal(new ObjectInputStream(inputStream));
            Assert.assertEquals(this, app.getToWriteObj());
        }
        catch (IOException | ClassNotFoundException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Override
    public int hashCode() {
        int result = testConfigureThreadsLogMaker.hashCode();
        result = 31 * result + exitApp.hashCode();
        result = 31 * result + messageToUser.hashCode();
        return result;
    }
    
    private void writeEx() {
        File file = new File(this.getClass().getSimpleName() + ".obj");
        try {
            Files.deleteIfExists(file.toPath());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Assert.assertFalse(file.exists());
        try (OutputStream outputStream = new FileOutputStream(file.getName())) {
            new ExitApp(this).writeExternal(new ObjectOutputStream(outputStream));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Assert.assertTrue(file.exists());
        readExt(file);
    
    }
    
    @Test(enabled = false)
    public void testTestToString() {
        String toString = exitApp.toString();
        Assert.assertTrue(toString.contains("ExitApp{reasonExit='test'"), toString);
    }
    
    @Test(enabled = false)
    public void testGetVisitsMap() {
        Map<Long, Visitor> visitsMap = ExitApp.getVisitsMap();
        String fromArrayStr = new TForms().fromArray(visitsMap);
        Assert.assertTrue(fromArrayStr.contains("127.0.0.1"), fromArrayStr);
    }
}