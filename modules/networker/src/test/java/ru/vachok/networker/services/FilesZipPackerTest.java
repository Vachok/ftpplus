// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TestConfigure;


/**
 @see FilesZipPacker
 @since 21.06.2019 (20:15) */
@SuppressWarnings("ALL") public class FilesZipPackerTest {
    
    
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