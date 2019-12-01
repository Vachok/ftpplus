// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info.stats;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.FileNames;

import java.io.File;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;


/**
 @see ComputerUserResolvedStats
 @since 20.06.2019 (10:26) */
public class ComputerUserResolvedStatsTest {


    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());

    private ComputerUserResolvedStats computerUserResolvedStats;

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }

    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }

    @BeforeMethod
    public void initStats() {
        this.computerUserResolvedStats = new ComputerUserResolvedStats();
    }

    /**
     * @see ComputerUserResolvedStats#call()
     */
    @Test
    public void testCall() {
        ComputerUserResolvedStats computerUserResolvedStats = new ComputerUserResolvedStats();
        String call = computerUserResolvedStats.call();
        Assert.assertTrue(new File(FileNames.USERLOGINCOUNTER_TXT).lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1));
    }

    /**
     @see ComputerUserResolvedStats#selectFrom()
     */
    @Test
    public void testSelectFrom() {
        ComputerUserResolvedStats computerUserResolvedStats = new ComputerUserResolvedStats();
        int selectedRows = computerUserResolvedStats.selectFrom();
        Assert.assertTrue(selectedRows > 100, MessageFormat.format("selectedRows : {0}", selectedRows));
        Assert.assertTrue(new File(FileNames.VELKOMPCUSERAUTO_TXT).lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1));
    }

    @Test
    public void testRun() {
        computerUserResolvedStats.run();
    }

    @Test
    public void testGetInfo() {
        throw new InvokeEmptyMethodException("GetInfo created 01.12.2019 at 22:05");
    }

    @Test
    public void testGetInfoAbout() {
        throw new InvokeEmptyMethodException("GetInfoAbout created 01.12.2019 at 22:05");
    }

    @Test
    public void testTestToString() {
        throw new InvokeEmptyMethodException("TestToString created 01.12.2019 at 22:05");
    }
}