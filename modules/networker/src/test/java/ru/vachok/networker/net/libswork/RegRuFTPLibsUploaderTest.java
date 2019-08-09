// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.libswork;


import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.OtherKnownDevices;
import ru.vachok.networker.enums.PropertiesNames;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.Properties;


public class RegRuFTPLibsUploaderTest extends RegRuFTPLibsUploader {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void ftpTest() {
        LibsHelp libsHelp = new RegRuFTPLibsUploader();
        try {
            libsHelp.uploadLibs();
        }
        catch (AccessDeniedException | ConnectException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    @Test
    public void continueConnect() {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(getHost(), ConstantsFor.FTP_PORT);
            FTPClientConfig config = new FTPClientConfig();
            config.setServerTimeZoneId("Europe/Moscow");
            ftpClient.configure(config);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    @Test
    public void checkPassOverDB() {
        Properties properties = new DBRegProperties(PropertiesNames.PRID_PASS).getProps();
        String passDB = properties.getProperty(PropertiesNames.DEFPASSFTPMD5HASH);
        if (Arrays.equals(passDB.getBytes(), PASSWORD_HASH.getBytes())) {
            System.out.println("properties = " + properties.getProperty("realftppass"));
        }
    }
    
    @Test
    public void chkPC$$COPY() {
        Assert.assertTrue(UsefulUtilities.thisPC().toLowerCase().contains("home") || UsefulUtilities.thisPC().toLowerCase()
            .contains(OtherKnownDevices.DO0213_KUDR.split("\\Q.eat\\E")[0]));
        
    }
}