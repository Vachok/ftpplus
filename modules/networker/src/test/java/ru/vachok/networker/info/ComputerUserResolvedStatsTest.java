// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.FileNames;

import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 @see ComputerUserResolvedStats
 @since 20.06.2019 (10:26) */
public class ComputerUserResolvedStatsTest {
    
    
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
     * @see ComputerUserResolvedStats#call()
     */
    @Test
    public void testCall() {
        ComputerUserResolvedStats computerUserResolvedStats = new ComputerUserResolvedStats();
        String call = computerUserResolvedStats.call();
        Assert.assertTrue(new File("user_login_counter.txt").lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1));
    }
    
    /**
     @see ComputerUserResolvedStats#selectFrom()
     */
    @Test
    public void testSelectFrom() {
        ComputerUserResolvedStats computerUserResolvedStats = new ComputerUserResolvedStats();
        int selectedRows = computerUserResolvedStats.selectFrom();
        Assert.assertTrue(selectedRows > 100);
        Assert.assertTrue(new File(FileNames.FILENAME_VELKOMPCUSERAUTOTXT).lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1));
    }
}