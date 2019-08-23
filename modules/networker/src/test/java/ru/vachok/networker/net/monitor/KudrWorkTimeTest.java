// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.monitor;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.net.NetKeeper;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.DataConnectToAdapter;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.ssh.TemporaryFullInternet;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.*;
import java.util.concurrent.*;


/**
 @see KudrWorkTime
 @since 12.07.2019 (0:46) */
public class KudrWorkTimeTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private final File logFile = new File(this.getClass().getSimpleName() + ".res");
    
    private InetAddress samsIP;
    
    private InetAddress do0213IP;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private List<String> execList = NetKeeper.getKudrWorkTime();
    
    private NetScanService kudrService = new KudrWorkTime(true);
    
    private int startPlus9Hours = LocalTime.parse("17:30").toSecondOfDay();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @BeforeMethod
    public void initInetAddr() {
        try {
            
            this.samsIP = InetAddress.getByAddress(InetAddress.getByName("10.200.214.80").getAddress());
        }
        catch (UnknownHostException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void kudrMonitorTest() {
        Runnable runnable = kudrService.getMonitoringRunnable();
        Future<?> submit = Executors.newSingleThreadExecutor().submit(runnable);
        try {
            Object monitorResult = submit.get(1000, TimeUnit.MILLISECONDS);
            Assert.assertNull(monitorResult);
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetMapOfConditionsTypeNameTypeCondition() {
        Map<String, Object> condition = ((KudrWorkTime) kudrService).getMapOfConditionsTypeNameTypeCondition();
        Assert.assertNotNull(condition);
        Assert.assertTrue(condition.isEmpty());
    }
    
    @Test
    public void testPingDevices() {
        Map<InetAddress, String> devToPing = new HashMap<>();
        devToPing.put(InetAddress.getLoopbackAddress(), "local");
        List<String> pingedDevs = kudrService.pingDevices(devToPing);
        String fromArray = new TForms().fromArray(pingedDevs);
        Assert.assertTrue(fromArray.contains("Pinging local, with timeout 170 seconds - true"), fromArray);
    }
    
    @Test
    public void testIsReach() {
        boolean isReachIP = NetScanService.isReach(InetAddress.getLoopbackAddress().getHostAddress());
        Assert.assertTrue(isReachIP);
    }
    
    @Test(enabled = false)
    public void testGetExecution() {
        try {
            String execution = kudrService.getExecution();
            Thread.sleep(150);
            Assert.assertTrue(execution.contains("Starting monitor!"), execution);
        }
        catch (InvokeEmptyMethodException | InterruptedException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetPingResultStr() {
        try {
            String resultStr = kudrService.getPingResultStr();
            System.out.println("kudrService.getPingResultStr() = " + resultStr);
            Assert.assertTrue(resultStr.contains("starting"));
        }
        catch (InvokeEmptyMethodException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testWriteLog() {
        try {
            Future<String> future = Executors.newSingleThreadExecutor().submit(kudrService::writeLog);
            future.get(1, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetStatistics() {
        String statistics = kudrService.getStatistics();
        Assert.assertTrue(statistics.isEmpty(), statistics);
    }
    
    @Test
    public void testTestToString() {
        String toStr = kudrService.toString();
        Assert.assertTrue(toStr.contains("KudrWorkTime{logFile="), toStr);
    }
    
    @Test
    public void getExecution$$COPY() {
        execList.add(MessageFormat.format(KudrWorkTime.STARTING, LocalTime.now()));
        Future<?> submit = Executors.newSingleThreadExecutor().submit(this::monitorAddress$$COPY);
        try {
            int timeout = 5;
            Assert.assertNull(submit.get(timeout, TimeUnit.SECONDS));
        }
        catch (InterruptedException | ExecutionException e) {
            messageToUser.error(MessageFormat
                .format("KudrWorkTime.getExecution {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        catch (TimeoutException e) {
            Assert.assertNotNull(e);
        }
    }
    
    @Test(enabled = false)
    public void monitorAddress$$COPY() {
        this.startPlus9Hours = LocalTime.parse("18:30").toSecondOfDay() - LocalTime.now().toSecondOfDay();
        boolean isSamsOnline;
        do {
            isSamsOnline = isReach$$COPY(samsIP);
            if (isSamsOnline) {
                FileSystemWorker.appendObjectToFile(logFile, writeLog$$COPY());
                new TemporaryFullInternet(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(9)).run();
                break;
            }
        } while (true);
        
        doIsReach$$COPY();
    }
    
    private void doIsReach$$COPY() {
        final int start = LocalTime.now().toSecondOfDay();
        this.startPlus9Hours = (int) (start + TimeUnit.HOURS.toSeconds(9));
        boolean isDOOnline;
        do {
            isDOOnline = isReach$$COPY(do0213IP);
        } while (isDOOnline);
    }
    
    private boolean isReach$$COPY(@NotNull InetAddress address) {
        try {
            return address.isReachable((int) TimeUnit.SECONDS.toMillis(5));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            return false;
        }
    }
    
    private String writeLog$$COPY() {
        String sql = "INSERT INTO `u0466446_testing`.`worktime` (`Date`, `Timein`, `Timeout`) VALUES (?, ?, ?);";
        try (Connection c = DataConnectToAdapter.getRegRuMysqlLibConnection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement p = c.prepareStatement(sql)) {
                String dateToDB = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                p.setString(1, dateToDB);
                if (LocalTime.now().toSecondOfDay() >= startPlus9Hours) {
                    p.setLong(2, 0);
                    p.setLong(3, System.currentTimeMillis());
                }
                else {
                    p.setLong(2, System.currentTimeMillis());
                    p.setLong(3, 0);
                }
                int updDB = p.executeUpdate();
                String logStr = MessageFormat.format("{0} database updated. {1} time now", updDB, LocalTime.now().toString());
                execList.add(logStr);
                FileSystemWorker.appendObjectToFile(logFile, logStr);
                return logStr;
            }
        }
        catch (SQLException e) {
            execList.add(e.getMessage() + "\n");
            doIsReach$$COPY();
            return e.getMessage();
        }
    }
}