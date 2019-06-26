package ru.vachok.networker.net.libswork;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TestConfigure;

import java.net.ConnectException;
import java.nio.file.AccessDeniedException;


public class RegRuDBLibsTest {
    
    
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
    public void testConnectTo() {
        RegRuDBLibs regRuDBLibs = new RegRuDBLibs();
        System.out.println(regRuDBLibs.getVersion());
        try {
            System.out.println(regRuDBLibs.uploadLibs());
        }
        catch (AccessDeniedException | ConnectException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    @Test
    public void testGetContentsQueue() {
    }
}