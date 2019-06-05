package ru.vachok.networker.net.ftp;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.ConnectException;
import java.nio.file.AccessDeniedException;


public class RegRuDBLibsTest {
    
    
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