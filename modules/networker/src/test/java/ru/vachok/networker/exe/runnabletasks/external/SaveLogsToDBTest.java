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
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.text.MessageFormat;
import java.util.ConcurrentModificationException;
import java.util.concurrent.*;


/**
 @see SaveLogsToDB
 @since 14.06.2019 (16:55) */
@SuppressWarnings("ALL")
public class SaveLogsToDBTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private InformationFactory db = InformationFactory.getInstance(InformationFactory.STATS_INTERNET_SAVE_LOGS);
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 4));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void testShowInfo() {
        String info = String.valueOf(0);
        try {
            info = db.getInfo();
        }
        catch (ConcurrentModificationException e) {
            messageToUser.error(MessageFormat.format("SaveLogsToDBTest.testShowInfo {0} - {1}\nStack:\n{2}",
                    e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
    }
    
    @Test
    public void testTestToString() {
        String toStr = db.toString();
        Assert.assertTrue(toStr.contains("SaveLogsToDB["));
    }
    
    @Test
    public void testGetDBInfo() {
        String info = db.getInfo();
        System.out.println("info = " + info);
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
            String dbCallable = submit.get(100, TimeUnit.SECONDS);
            Assert.assertTrue(dbCallable.contains("access.log"), dbCallable);
        }
        catch (TimeoutException | ExecutionException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (InterruptedException e) {
            messageToUser.error(MessageFormat
                .format("SaveLogsToDBTest.testCall {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
    }
}