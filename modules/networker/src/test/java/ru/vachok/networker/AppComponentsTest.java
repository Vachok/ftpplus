// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.VersionInfo;
import ru.vachok.networker.componentsrepo.Visitor;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;


public class AppComponentsTest {
    
    
    @Test
    public void testGetProps() {
        Properties appProps = AppComponents.getProps();
        Assert.assertTrue(appProps.size() > 10);
    }
    
    @Test
    public void testIpFlushDNS() {
        try {
            String cp866 = new String(AppComponents.ipFlushDNS().getBytes(), "CP866");
            Assert.assertTrue(cp866.contains("DNS"));
            
        }
        catch (UnsupportedEncodingException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    @Test
    public void testConnection() {
    }
    
    @Test
    public void testSimpleCalculator() {
    }
    
    @Test
    public void testSshActs() {
    }
    
    @Test
    public void testVisitor() {
        HttpServletRequest request = new MockHttpServletRequest();
        Visitor visitor = new Visitor(request);
        Map<Long, Visitor> map = ExitApp.getVisitsMap();
        long timeSession = request.getSession().getCreationTime();
        map.put(timeSession, visitor);
        Assert.assertNotNull(map.get(timeSession));
    }
    
    @Test
    public void testSaveLogsToDB() {
    }
    
    @Test
    public void testThreadConfig() {
    }
    
    @Test
    public void testNetScannerSvc() {
    }
    
    @Test
    public void testVersionInfo() {
        VersionInfo versionInfo = AppComponents.versionInfo();
        Assert.assertNotNull(versionInfo);
        Assert.assertFalse(versionInfo.toString().isEmpty());
    }
    
    @Test
    public void testAdSrv() {
    }
    
    @Test
    public void testConfigurableApplicationContext() {
    }
    
    @Test
    public void testUpdateProps() {
    }
    
    @Test
    public void testUpdateProps1() {
    }
    
    @Test
    public void testDiapazonedScanInfo() {
    }
    
    @Test
    public void testDiapazonScan() {
    }
    
    @Test
    public void testGetLogger() {
    }
    
    @Test
    public void testScanOline() {
    }
    
    @Test
    public void testTemporaryFullInternet() {
    }
    
    @Test
    public void testLaunchRegRuFTPLibsUploader() {
    }
    
    @Test
    public void testGetUserPref() {
    }
}