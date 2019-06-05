// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.libswork;


import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.networker.ConstantsFor;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.AccessDeniedException;


public class RegRuFTPLibsUploaderTest extends RegRuFTPLibsUploader {
    
    
    @Test()
    public void ftpTest() {
        LibsHelp libsHelp = new RegRuFTPLibsUploader();
        try {
            libsHelp.uploadLibs();
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
    
    @Test
    public void continueConnect() {
        FTPClient ftpClient = new FTPClient();
        String dbPass = new DBRegProperties("general-pass").getProps().getProperty("realftppass");
        setFtpPass(dbPass);
        try {
            ftpClient.connect(super.getHost(), ConstantsFor.FTP_PORT);
            FTPClientConfig config = new FTPClientConfig();
            config.setServerTimeZoneId("Europe/Moscow");
            ftpClient.configure(config);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
}