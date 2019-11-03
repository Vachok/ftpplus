package ru.vachok.networker.net.monitor;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.*;


/**
 @see NetMonitorPTV
 @since 01.11.2019 (8:49) */
public class NetMonitorPTVTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private NetMonitorPTV netMonitorPTV;
    
    private File file;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
        this.file = new File(FileNames.PING_TV);
        if (file.exists()) {
            Assert.assertTrue(file.delete());
        }
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @BeforeMethod
    public void initPTV() {
        this.netMonitorPTV = new NetMonitorPTV();
    }
    
    @Test
    public void testGetExecution() {
        String ptvExecution = netMonitorPTV.getExecution();
        Assert.assertTrue(ptvExecution.contains("Bytes in stream"), ptvExecution);
    }
    
    @Test
    public void testGetPingResultStr() {
        String pingResultStr = netMonitorPTV.getPingResultStr();
        Assert.assertEquals(pingResultStr, "No pings yet.");
    }
    
    @Test
    public void testWriteLog() {
        try {
            String writeLog = netMonitorPTV.writeLog();
            System.out.println("writeLog = " + writeLog);
        }
        catch (RuntimeException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        
    }
    
    @Test
    public void testGetMonitoringRunnable() {
        Runnable monitoringRunnable = netMonitorPTV.getMonitoringRunnable();
        ForkJoinPool forkJoinPool = new ForkJoinPool(3);
        ForkJoinTask<?> forkJoinTask = forkJoinPool.submit(monitoringRunnable);
        try {
            forkJoinTask.get(30, TimeUnit.SECONDS);
            Assert.assertTrue(file.exists());
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        catch (TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void testGetStatistics() {
        try {
            String ptvStatistics = netMonitorPTV.getStatistics();
            System.out.println("ptvStatistics = " + ptvStatistics);
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void testGetPingResultLast() {
        String pingResultLast = netMonitorPTV.getPingResultLast();
        Assert.assertEquals(pingResultLast, "No pings yet.");
    }
    
    @Test
    public void testTestToString() {
        String toString = netMonitorPTV.toString();
        Assert.assertEquals(toString, "NetMonitorPTV{pingResultLast='No pings yet.'}");
    }
    
    @Test
    public void newLogicTest() {
        DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.H2DB);
        String toString = dataConnectTo.toString();
        Assert.assertEquals(toString, "H2DB{}");
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DB_LANMONITOR)) {
            String url = connection.getMetaData().getURL();
            Assert.assertEquals(url, "jdbc:h2:mem:lan.monitor");
            Assert.assertTrue(connection.isValid(5));
            try (PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS monitor (\n" +
                    "\t`idRec` INT(6) NOT NULL AUTO_INCREMENT,\n" +
                    "\t`ip` VARCHAR(16) NOT NULL DEFAULT '127.0.0.1',\n" +
                    "\t`pcName` VARCHAR(50) NOT NULL DEFAULT 'unresolved',\n" +
                    "\t`online` ENUM('true','false') NOT NULL DEFAULT 'false',\n" +
                    "\t`tstamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
                    "\tPRIMARY KEY (`idRec`),\n" +
                    "\tUNIQUE INDEX `ip_pcName` (`ip`, `pcName`)\n" +
                    ")\n" +
                    "ENGINE=InnoDB\n" +
                    ";\n")) {
                preparedStatement.executeUpdate();
                pingOnePc(connection);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    private void pingOnePc(@NotNull Connection connection) {
        final String sql = "INSERT INTO monitor (`ip`, `pcName`, `online`) VALUES (?,?,?);";
        String ipAddr = "10.200.213.85";
        NameOrIPChecker checker = new NameOrIPChecker(ipAddr);
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, ipAddr);
            preparedStatement.setString(2, checker.resolveInetAddress().getHostName());
            preparedStatement.setString(3, String.valueOf(NetScanService.isReach(ipAddr)));
            preparedStatement.executeUpdate();
            try (PreparedStatement checkInfo = connection.prepareStatement("select * from monitor");
                 ResultSet resultSet = checkInfo.executeQuery()) {
                while (resultSet.next()) {
                    String ip = resultSet.getString("ip");
                    Assert.assertEquals(ip, "10.200.213.85");
                    String pcName = resultSet.getString("pcName");
                    String online = resultSet.getString("online");
                    long stamp = resultSet.getTimestamp(ConstantsFor.DBFIELD_TSTAMP).getTime();
                    Assert.assertTrue(stamp > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10)), new Date(stamp).toString());
                    System.out.println(MessageFormat.format("{3}) pcName = {0} is online: {1} at {2}", pcName, online, new Date(stamp), resultSet.getInt("idRec")));
                }
            }
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
}