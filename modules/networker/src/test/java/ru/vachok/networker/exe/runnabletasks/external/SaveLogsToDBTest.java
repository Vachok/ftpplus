// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks.external;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


/**
 @since 14.06.2019 (16:55) */
@SuppressWarnings("ALL")
public class SaveLogsToDBTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private InformationFactory db = new SaveLogsToDB();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void testShowInfo() {
        int info = ((SaveLogsToDB) db).showInfo();
        Assert.assertTrue(info > 100);
    }
    
    @Test
    public void testTestToString() {
        String toStr = db.toString();
        Assert.assertTrue(toStr.contains("SaveLogsToDB["));
    }
    
    @Test
    public void testGetDBInfo() {
        int info = ((SaveLogsToDB) db).getDBInfo();
        Assert.assertTrue(info > 100);
    }
    
    @Test
    public void testGetInfoAbout() {
        String infoAbout = db.getInfoAbout("40");
        System.out.println("infoAbout = " + infoAbout);
    }
    
    @Test
    public void testCall() {
        try {
            Future<String> submit = Executors.newSingleThreadExecutor().submit((Callable<String>) db);
            String dbCallable = submit.get(30, TimeUnit.SECONDS);
            Assert.assertTrue(dbCallable.contains("access.log"), dbCallable);
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}