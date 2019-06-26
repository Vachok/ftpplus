// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see FilesZipPacker
 @since 21.06.2019 (20:15) */
@SuppressWarnings("ALL") public class FilesZipPackerTest {
    
    
    private final TestConfigureThreadsLogMaker testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
    }
    
    
    @Test
    public void testCall() {
        FilesZipPacker filesZipPacker = new FilesZipPacker();
        try {
            filesZipPacker.call();
        }
        catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }
}