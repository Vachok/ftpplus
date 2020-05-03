// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info.stats;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
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
     @see ComputerUserResolvedStats#call()
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
        int selectedRows = 0;
        try {
            selectedRows = computerUserResolvedStats.selectFrom();
        }
        catch (InvokeIllegalException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        Assert.assertTrue(selectedRows > 100, MessageFormat.format("selectedRows : {0}", selectedRows));
        Assert.assertTrue(new File(FileNames.VELKOMPCUSERAUTO_TXT).lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1));
    }

    @Test
    public void testRun() {
        computerUserResolvedStats.run();
    }

    @Test
    public void testGetInfo() {
        String statsInfo = computerUserResolvedStats.getInfo();
        Assert.assertTrue(statsInfo.contains("total pc:"), statsInfo);
        Assert.assertTrue(Integer.parseInt(statsInfo.split(": ")[1]) > 0);
    }

    @Test
    public void testGetInfoAbout() {
        String statsInfoAbout = computerUserResolvedStats.getInfoAbout("");
        Assert.assertTrue(statsInfoAbout.contains("total pc:"), statsInfoAbout);
    }

    @Test
    public void testTestToString() {
        String toString = computerUserResolvedStats.toString();
        Assert.assertTrue(toString.contains("ComputerUserResolvedStats["));
    }
}