// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.awt.*;
import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 @see PCStats
 @since 20.06.2019 (10:26) */
public class PCStatsTest {
    
    
    private final TestConfigureThreadsLogMaker testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    
    /**
     * @see PCStats#call()
     */
    @Test
    public void testCall() {
        PCStats pcStats = new PCStats();
        String call = pcStats.call();
        Assert.assertTrue(new File("user_login_counter.txt").lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1));
    }
    
    /**
     @see PCStats#selectFrom()
     */
    @Test
    public void testSelectFrom() {
        PCStats pcStats = new PCStats();
        int selectedRows = pcStats.selectFrom();
        Assert.assertTrue(selectedRows > 100);
        Assert.assertTrue(new File(ConstantsFor.FILENAME_VELKOMPCUSERAUTOTXT).lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1));
    }
    
    /**
     @see PCStats#insertTo()
     */
    @Test
    public void testInsertTo() {
        try {
            new PCStats().insertTo();
        }
        catch (IllegalComponentStateException e) {
            Assert.assertNotNull(e);
        }
    }
    
    /**
     @see PCStats#deleteFrom()
     */
    @Test
    public void testDeleteFrom() {
        try {
            new PCStats().deleteFrom();
        }
        catch (IllegalComponentStateException e) {
            Assert.assertNotNull(e);
        }
    }
    
    /**
     * @see PCStats#updateTable()
     */
    @Test
    public void testUpdateTable() {
        try {
            new PCStats().updateTable();
        }
        catch (IllegalComponentStateException e) {
            Assert.assertNotNull(e);
        }
    }
}