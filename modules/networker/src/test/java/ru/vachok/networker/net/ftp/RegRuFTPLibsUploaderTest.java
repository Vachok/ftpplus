// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.ftp;


import org.testng.Assert;
import org.testng.annotations.Test;

import javax.naming.AuthenticationException;
import java.net.ConnectException;
import java.nio.file.AccessDeniedException;


public class RegRuFTPLibsUploaderTest {
    
    
    @Test()
    public void ftpTest() {
        FTPHelper ftpHelper = new RegRuFTPLibsUploader();
        try {
            ftpHelper.connectTo();
        }
        catch (AccessDeniedException | ConnectException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".ftpTest");
        }
    }
    
    @Test
    public void chkPassTest() {
        String ftpPass = null;
        try {
            ftpPass = new RegRuFTPLibsUploader().chkPass();
        }
        catch (AuthenticationException e) {
            Assert.assertNull(e, e.getMessage());
        }
        Assert.assertNotNull(ftpPass);
        System.out.println("ftpPass = " + ftpPass);
    }
}