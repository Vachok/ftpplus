// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.nio.file.Path;
import java.nio.file.Paths;


/**
 @see FileSystemWorker
 @since 23.06.2019 (9:44) */
@SuppressWarnings("ALL") public class FileSystemWorkerTest extends FileSystemWorker {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
    }
    /**
     @see FileSystemWorker#countStringsInFile(Path)
     */
    @Test
    public void testCountStringsInFile() {
        String fileSeparator = System.getProperty("file.separator");
        Path fileToCount = Paths.get("\\\\srv-fs\\Common_new\\14_ИТ_служба\\Внутренняя\\common.own").normalize();
        int stringsInMaxOnline = FileSystemWorker.countStringsInFile(fileToCount);
        Assert.assertTrue(stringsInMaxOnline > 50, stringsInMaxOnline + " strings in " + fileToCount.toFile().getName());
    }
}