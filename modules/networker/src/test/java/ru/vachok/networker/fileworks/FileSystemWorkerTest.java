// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TestConfigure;

import java.nio.file.Path;
import java.nio.file.Paths;


/**
 @see FileSystemWorker
 @since 23.06.2019 (9:44) */
@SuppressWarnings("ALL") public class FileSystemWorkerTest extends FileSystemWorker {
    
    
    private final TestConfigure testConfigure = new TestConfigure(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigure.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigure.afterClass();
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