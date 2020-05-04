package ru.vachok.networker.net.monitor;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.info.NetScanService;

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
@Ignore
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
    @Ignore
    public void testGetExecution() {
        String ptvExecution = netMonitorPTV.getExecution();
        Assert.assertTrue(new File(FileNames.PING_TV).exists());
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

    private void pingOnePc(@NotNull Connection connection) {
        final String sql = "INSERT INTO monitor (`ip`, `pcName`, `online`) VALUES (?,?,?);";
        String ipAddr = "10.200.213.85";
        NameOrIPChecker checker = new NameOrIPChecker(ipAddr);

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, ipAddr);
            String hostName = checker.resolveInetAddress().getHostName();
            if (hostName.matches(ConstantsFor.PATTERN_IP.pattern())) {
                hostName = MessageFormat.format("not resolved (by {0})", UsefulUtilities.thisPC());
            }
            preparedStatement.setString(2, hostName);
            preparedStatement.setString(3, String.valueOf(NetScanService.isReach(ipAddr)));
            preparedStatement.executeUpdate();
            try (PreparedStatement checkInfo = connection.prepareStatement("select * from monitor");
                 ResultSet resultSet = checkInfo.executeQuery()) {
                while (resultSet.next()) {
                    String ip = resultSet.getString("ip");
                    Assert.assertEquals(ip, "10.200.213.85");
                    String pcName = resultSet.getString("pcName");
                    String online = resultSet.getString(ConstantsFor.DBFIELD_ONLINE);
                    long stamp = resultSet.getTimestamp(ConstantsFor.DBFIELD_TSTAMP).getTime();
                    Assert.assertTrue(stamp > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10)), new Date(stamp).toString());
                    System.out.println(MessageFormat.format("{3}) pcName = {0} is online: {1} at {2}", pcName, online, new Date(stamp), resultSet.getInt("idRec")));
                }
            }
        }
        catch (SQLException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
}