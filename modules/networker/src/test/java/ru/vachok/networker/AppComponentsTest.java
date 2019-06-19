// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.sysinfo.VersionInfo;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


@SuppressWarnings("ALL") public class AppComponentsTest {
    
    
    @Test
    public void testGetProps() {
        Properties appProps = AppComponentsTest.getPropsTESTCOPY();
        Assert.assertTrue(appProps.size() > 5, "AppProps size = " + appProps.size());
        Assert.assertTrue(appProps.getProperty("server.port").equals("8880"));
        Assert.assertTrue(appProps.getProperty("application.name").equals("ru.vachok.networker-"));
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
        try {
            Connection webapp = new AppComponents().connection("u0466446_webapp");
            boolean isValid = webapp.isValid(1000);
            Assert.assertTrue(isValid);
            webapp.close();
            Assert.assertTrue(webapp.isClosed());
        }
        catch (IOException | SQLException e) {
            Assert.assertNull(e, e.getMessage());
        }
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
    public void testVersionInfo() {
        VersionInfo versionInfo = AppComponents.versionInfo();
        Assert.assertNotNull(versionInfo);
        Assert.assertFalse(versionInfo.toString().isEmpty());
    }
    
    @Test
    public void testConfigurableApplicationContext() {
        try {
            AppComponents.configurableApplicationContext();
        }
        catch (IllegalComponentStateException e) {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void testUpdateProps() {
        InitProperties initProperties = new FileProps(ConstantsFor.class.getSimpleName());
        Properties props = initProperties.getProps();
        Assert.assertTrue(props.size() > 5, new TForms().fromArray(props, false));
        Path libsPath = Paths.get("lib/stats-8.0.1920.jar").toAbsolutePath().normalize();
        try {
            boolean isUpdate = new AppComponents().updateProps(props);
            Assert.assertTrue(isUpdate);
            
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    @Test
    public void testUpdateProps1() {
        try {
            new AppComponents().updateProps();
        }
        catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
        InitProperties initProperties = new FileProps(ConstantsFor.class.getSimpleName());
        try {
            new AppComponents().updateProps(initProperties.getProps());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    @Test
    public void testDiapazonedScanInfo() {
        try {
            String diapazonInfo = DiapazonScan.getInstance().theInfoToString();
            System.out.println(diapazonInfo);
            Assert.assertTrue(diapazonInfo.contains("a href=\"/showalldev\""), diapazonInfo);
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        
    }
    
    @Test
    public void testLaunchRegRuFTPLibsUploader() {
        String ftpLibUplString = new AppComponents().launchRegRuFTPLibsUploader();
        Assert.assertTrue(ftpLibUplString.contains("true"));
    }
    
    @Test
    public void testGetUserPref() {
        Preferences pref = AppComponents.getUserPref();
        try {
            Assert.assertNotNull(pref.keys());
            Assert.assertTrue(pref.keys().length > 2);
        }
        catch (BackingStoreException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    private static Properties getPropsTESTCOPY() {
        final Properties APP_PR = new Properties();
        /*      */
        
        File fileProps = new File(ConstantsFor.class.getSimpleName() + ConstantsFor.FILEEXT_PROPERTIES);
        
        if (APP_PR.size() > 3) {
            if ((APP_PR.getProperty(ConstantsFor.PR_DBSTAMP) != null) && (Long.parseLong(APP_PR.getProperty(ConstantsFor.PR_DBSTAMP)) + TimeUnit.MINUTES.toMillis(180)) < System
                .currentTimeMillis()) {
                APP_PR.putAll(new AppComponentsTest().getAppPropsTESTCOPY());
            }
            Assert.assertTrue(APP_PR.size() > 3);
            System.out.println(new TForms().fromArray(APP_PR, false));
        }
        if (fileProps.exists() & !fileProps.canWrite()) {
            InitProperties initProperties = new FileProps(ConstantsFor.class.getSimpleName());
            APP_PR.clear();
            APP_PR.putAll(initProperties.getProps());
            APP_PR.setProperty("dbstamp", String.valueOf(System.currentTimeMillis()));
            initProperties.setProps(APP_PR);
            initProperties = new DBRegProperties(ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName());
            initProperties.delProps();
            initProperties.setProps(APP_PR);
        }
        else {
            Properties appProps = new AppComponentsTest().getAppPropsTESTCOPY();
            APP_PR.setProperty(ConstantsFor.PR_DBSTAMP, String.valueOf(System.currentTimeMillis()));
            APP_PR.setProperty(ConstantsFor.PR_THISPC, ConstantsFor.thisPC());
            APP_PR.putAll(appProps);
        }
        return APP_PR;
    }
    
    private Properties getAppPropsTESTCOPY() {
        final String DB_JAVA_ID = ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName();
        final Properties APP_PR = new Properties();
        /*      */
        
        MysqlDataSource mysqlDataSource = new DBRegProperties(DB_JAVA_ID).getRegSourceForProperties();
        mysqlDataSource.setRelaxAutoCommit(true);
        mysqlDataSource.setLogger("java.util.Logger");
        Callable<Properties> theProphecy = new DBPropsCallable(mysqlDataSource, APP_PR);
        try {
            APP_PR.putAll(theProphecy.call());
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage());
        }
        return APP_PR;
    }
    
    
}