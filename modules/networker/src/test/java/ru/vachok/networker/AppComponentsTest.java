// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.accesscontrol.PfLists;
import ru.vachok.networker.accesscontrol.sshactions.SshActs;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.enums.UsefulUtilites;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.runnabletasks.TemporaryFullInternet;
import ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.net.PCUserResolver;
import ru.vachok.networker.net.scanner.ScanOnline;
import ru.vachok.networker.restapi.database.DataConnectToAdapter;
import ru.vachok.networker.restapi.props.DBPropsCallable;
import ru.vachok.networker.restapi.props.FilePropsLocal;
import ru.vachok.networker.services.SimpleCalculator;
import ru.vachok.networker.sysinfo.VersionInfo;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @see AppComponents */
@SuppressWarnings("ALL")
public class AppComponentsTest {
    
    
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
    public void testGetMailProps() {
        Properties props = AppComponents.getMailProps();
        Assert.assertTrue(props.size() > 3);
        Assert.assertEquals(props.getProperty("host"), "srv-mail3.eatmeat.ru");
    }
    
    @Test
    public void testGetProps() {
        Properties appProps = new AppComponents().getProps();
        Assert.assertTrue(appProps.size() > 12, "AppProps size = " + appProps.size());
        Assert.assertTrue(appProps.getProperty("server.port").equals("8880"));
        Assert.assertTrue(appProps.getProperty("application.name").equals("ru.vachok.networker-"));
    }
    
    @Test
    public void testIpFlushDNS() {
        try {
            String cp866 = new String(AppComponents.ipFlushDNS().getBytes(), "CP866");
            Assert.assertTrue(cp866.contains("DNS"), cp866);
            
        }
        catch (UnsupportedEncodingException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    @Test
    public void testConnection() {
        MysqlDataSource mysqlDataSource = DataConnectToAdapter.getLibDataSource();
        Properties properties = new FilePropsLocal(ConstantsFor.class.getSimpleName()).getProps();
        StringBuilder stringBuilder = new StringBuilder();
        mysqlDataSource.setUser(properties.getProperty(PropertiesNames.PR_DBUSER));
        mysqlDataSource.setPassword(properties.getProperty(PropertiesNames.PR_DBPASS));
        mysqlDataSource.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_TESTING);
        mysqlDataSource.setAutoReconnect(true);
        try {
            Connection sourceConnection = mysqlDataSource.getConnection();
            DatabaseMetaData metaData = sourceConnection.getMetaData();
            ResultSet dataCatalogs = metaData.getCatalogs();
            while (dataCatalogs.next()) {
                stringBuilder.append(dataCatalogs.getString(1));
            }
            Assert.assertTrue(stringBuilder.toString().contains(ConstantsFor.DBBASENAME_U0466446_TESTING), stringBuilder.toString());
            sourceConnection.close();
            Assert.assertTrue(sourceConnection.isClosed());
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
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
    
    @Test(enabled = false)
    public void testConfigurableApplicationContext() {
        try {
            configurableApplicationContext();
        }
        catch (IllegalComponentStateException e) {
            Assert.assertNotNull(e);
        }
    }
    
    @Test(enabled = false)
    public void testUpdateProps() {
        InitProperties initProperties = new FileProps(ConstantsFor.class.getSimpleName());
        Properties props = initProperties.getProps();
        Assert.assertTrue(props.size() > 5, new TForms().fromArray(props, false));
        Path libsPath = Paths.get("lib/stats-8.0.1920.jar").toAbsolutePath().normalize();
    }
    
    @Test
    public void testDiapazonedScanInfo() {
        try {
            DiapazonScan instance = DiapazonScan.getInstance();
            String diapazonInfo = instance.getExecution();
            Assert.assertTrue(diapazonInfo.contains("a href=\"/showalldev\""), diapazonInfo);
            Assert.assertTrue(instance.getStatistics().contains("12 SpecVersion"), instance.getStatistics());
        }
        catch (TaskRejectedException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
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
    
    @Contract(" -> fail")
    public static AbstractBeanFactoryBasedTargetSource configurableApplicationContext() {
        throw new IllegalComponentStateException("Moved to: " + IntoApplication.class.getSimpleName());
    }
    
    @Test
    public void testLoadPropsAndWriteToFile() {
        new AppComponents().loadPropsAndWriteToFile();
        File propsFile = new File(ConstantsFor.class.getSimpleName() + FileNames.FILEEXT_PROPERTIES);
        Assert.assertTrue(propsFile.lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(ConstantsFor.DELAY)));
    }
    
    @Test
    public void testSimpleCalculator() {
        SimpleCalculator simpleCalculator = new AppComponents().simpleCalculator();
        String stampFromDate = simpleCalculator.getStampFromDate("07-01-1984-02-00");
        Assert.assertEquals(stampFromDate, "442278000000");
    }
    
    @Test
    public void testSshActs() {
        SshActs acts = new AppComponents().sshActs();
        String actsInet = acts.getInet();
        Assert.assertNull(actsInet);
    }
    
    @Test
    public void testSaveLogsToDB() {
        SaveLogsToDB toDB = new AppComponents().saveLogsToDB();
        String showInfo = toDB.showInfo();
        Assert.assertTrue(showInfo.contains("LOGS_TO_DB_EXT.showInfo"), showInfo);
    }
    
    @Test
    public void testThreadConfig() {
        ThreadConfig threadConfig = AppComponents.threadConfig();
        String toStr = threadConfig.toString();
        Assert.assertTrue(toStr.contains("ThreadConfig{java.util.concurrent.ThreadPoolExecutor"), toStr);
    }
    
    @Test
    public void testNetScannerSvc() {
        InformationFactory netScannerSvc = AppComponents.netScannerSvc();
        String toStr = netScannerSvc.toString();
        Assert.assertTrue(toStr.contains("NetScannerSvc{"), toStr);
    }
    
    @Test
    public void testAdSrv() {
        ADSrv adSrv = AppComponents.adSrv();
        String toStr = adSrv.toString();
        Assert.assertTrue(toStr.contains("ADSrv{CLASS_NAME_PCUSERRESOLVER='PCUserResolver'"), toStr);
    }
    
    @Test
    public void testScanOnline() {
        ScanOnline scanOnline = new AppComponents().scanOnline();
        boolean condition = scanOnline.isReach(InetAddress.getLoopbackAddress());
        Assert.assertFalse(condition);
        try {
            condition = scanOnline.isReach(InetAddress.getByAddress(InetAddress.getByName("10.200.213.1").getAddress()));
            Assert.assertTrue(condition);
            condition = scanOnline.isReach(InetAddress.getByAddress(InetAddress.getByName("8.8.8.8").getAddress()));
            Assert.assertTrue(condition);
        }
        catch (UnknownHostException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    
    }
    
    @Test
    public void testGetPFLists() {
        PfLists pfLists = new AppComponents().getPFLists();
        String toStr = pfLists.toString();
        Assert.assertTrue(toStr.contains("PfLists{fullSquid"), toStr);
    }
    
    @Test
    public void testGetUserResolver() {
        PCUserResolver userResolver = new AppComponents().getUserResolver("do0045");
        String infoAbout = userResolver.getInfoAbout();
        Assert.assertFalse(infoAbout.isEmpty());
        File fileTMP = new File(infoAbout.split(" ")[0]);
        Assert.assertTrue(fileTMP.exists());
        String readFileToStr = FileSystemWorker.readFile(fileTMP.getAbsolutePath());
        Assert.assertTrue(readFileToStr.contains("Bytes in stream"), readFileToStr);
    }
    
    @Test
    public void testTestToString() {
        String toStr = new AppComponents().toString();
        Assert.assertTrue(toStr.contains("Nothing to show..."), toStr);
    }
    
    @Test
    public void testTemporaryFullInternet() {
        TemporaryFullInternet fullInternet = new AppComponents().temporaryFullInternet();
        String toStr = fullInternet.toString();
        Assert.assertTrue(toStr.contains("TemporaryFullInternet{delStamp="), toStr);
    }
    
    public static Preferences getUserPref$$COPY() {
        return AppComponents.prefsNeededNode();
    }
    
    @NotNull
    private static Properties getPropsTESTCOPY() {
        final Properties APP_PR = new Properties();
        /*      */
        
        File fileProps = new File(ConstantsFor.class.getSimpleName() + FileNames.FILEEXT_PROPERTIES);
        
        if (APP_PR.size() > 3) {
            if ((APP_PR.getProperty(PropertiesNames.PR_DBSTAMP) != null) && (Long.parseLong(APP_PR.getProperty(PropertiesNames.PR_DBSTAMP)) + TimeUnit.MINUTES
                .toMillis(180)) < System
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
            APP_PR.setProperty(PropertiesNames.PR_DBSTAMP, String.valueOf(System.currentTimeMillis()));
            initProperties.setProps(APP_PR);
            initProperties = new DBRegProperties(ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName());
            initProperties.delProps();
            initProperties.setProps(APP_PR);
        }
        else {
            Properties appProps = new AppComponentsTest().getAppPropsTESTCOPY();
            APP_PR.setProperty(PropertiesNames.PR_DBSTAMP, String.valueOf(System.currentTimeMillis()));
            APP_PR.setProperty(PropertiesNames.PR_THISPC, UsefulUtilites.thisPC());
            APP_PR.putAll(appProps);
        }
        return APP_PR;
    }
    
    @NotNull
    private Properties getAppPropsTESTCOPY() {
        final String DB_JAVA_ID = ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName();
        final Properties APP_PR = new Properties();
        Callable<Properties> theProphecy = new DBPropsCallable();
        try {
            APP_PR.putAll(theProphecy.call());
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage());
        }
        return APP_PR;
    }
    
    
}