package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.NotNull;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.user.UserInfo;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.text.*;
import java.time.LocalTime;
import java.util.Date;
import java.util.*;
import java.util.concurrent.*;


/**
 @see PcNamesScanner */
public class PcNamesScannerTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(PcNamesScannerTest.class.getSimpleName(), System.nanoTime());
    
    private static final String name = "velkom.velkompc";
    
    private PcNamesScanner pcNamesScanner = new PcNamesScanner();
    
    private NetScanCtr netScanCtr = new NetScanCtr(new PcNamesScanner());
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 2));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        this.pcNamesScanner.setClassOption(netScanCtr);
        this.pcNamesScanner.setThePc("do0001");
        try {
            Files.deleteIfExists(new File(FileNames.SCAN_TMP).toPath());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        this.netScanCtr.setModel(new ExtendedModelMap());
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
        String killAll = AppComponents.threadConfig().killAll();
        messageToUser.warn(killAll);
    }
    
    @Test
    public void testToString() {
        String toStr = pcNamesScanner.toString();
        Assert.assertTrue(toStr.contains("PcNamesScanner{"), toStr);
        System.out.println("toStr = " + toStr);
    }
    
    @Test
    public void scanDO() {
        scanAutoPC("do", 4);
    }
    
    private void scanAutoPC(String testPrefix, int countPC) {
        final long startMethTime = System.currentTimeMillis();
        String pcsString;
        Collection<String> autoPcNames = new ArrayList<>(getCycleNames(testPrefix, countPC));
        for (String pcName : autoPcNames) {
            InformationFactory informationFactory = InformationFactory.getInstance(pcName);
            informationFactory.getInfo();
        }
        prefixToMap(testPrefix);
        UserInfo.writeUsersToDBFromSET();
        String elapsedTime = "<b>Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethTime) + " sec.</b> " + LocalTime.now();
        NetKeeper.getPcNamesForSendToDatabase().add(elapsedTime);
    }
    
    private @NotNull List<String> getCycleNames(String namePCPrefix, int countPC) {
        if (namePCPrefix == null) {
            namePCPrefix = "pp";
        }
        String nameCount;
        List<String> list = new ArrayList<>();
        int pcNum = 0;
        for (int i = 1; i < countPC; i++) {
            if (namePCPrefix.equals("no") || namePCPrefix.equals("pp") || namePCPrefix.equals("do") || namePCPrefix.equals("notd") || namePCPrefix.equals("dotd")) {
                nameCount = String.format("%04d", ++pcNum);
            }
            else {
                nameCount = String.format("%03d", ++pcNum);
            }
            list.add(namePCPrefix + nameCount + ConstantsFor.DOMAIN_EATMEATRU);
        }
        return list;
    }
    
    private void prefixToMap(String prefixPcName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<h4>");
        stringBuilder.append(prefixPcName);
        stringBuilder.append("     ");
        stringBuilder.append(NetKeeper.getPcNamesForSendToDatabase().size());
        stringBuilder.append("</h4>");
        String netMapKey = stringBuilder.toString();
    }
    
    @Test
    public void testIsTime() {
        try {
            Files.deleteIfExists(new File(FileNames.SCAN_TMP).toPath());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        pcNamesScanner.setModel(new ExtendedModelMap());
        pcNamesScanner.setRequest(new MockHttpServletRequest());
        Future<?> submit = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(()->pcNamesScanner.isTime());
        try {
            submit.get(20, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        checkWeekDB();
    }
    
    private static void checkWeekDB() {
        try (Connection connection = DataConnectTo.getDefaultI().getDefaultConnection(name);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM pcuserauto order by 'whenQueried' desc LIMIT 1;");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                if (resultSet.first()) {
                    Assert.assertTrue(checkDateFromDB(resultSet.getString("whenQueried")));
                    break;
                }
            }
        }
        catch (SQLException | ParseException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    private static boolean checkDateFromDB(String timeNow) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale.forLanguageTag("ru, RU"));
        Date parseDate = format.parse(timeNow);
        System.out.println("parseDate = " + parseDate);
        return parseDate.getTime() > (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30));
    }
    
    @Test
    public void testOnePrefixSET() {
        NetKeeper.getPcNamesForSendToDatabase().clear();
        Set<String> notdScanned = pcNamesScanner.onePrefixSET("notd");
        String setStr = new TForms().fromArray(notdScanned);
        Assert.assertTrue(setStr.contains(ConstantsFor.ELAPSED), setStr);
    }
    
    @Test
    public void testGetMonitoringRunnable() {
        Runnable runnable = pcNamesScanner.getMonitoringRunnable();
        Assert.assertNotEquals(runnable, pcNamesScanner);
        String runToStr = runnable.toString();
        Assert.assertTrue(runToStr.contains("ScannerUSR{"), runToStr);
        Future<?> submit = Executors.newSingleThreadExecutor().submit(runnable);
        try {
            submit.get(20, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        Assert.assertTrue(checkMap(), new TForms().fromArray(NetKeeper.getUsersScanWebModelMapWithHTMLLinks()));
        checkBigDB();
    }
    
    private static boolean checkMap() {
        ConcurrentNavigableMap<String, Boolean> htmlLinks = NetKeeper.getUsersScanWebModelMapWithHTMLLinks();
        String fromArray = new TForms().fromArray(htmlLinks);
        return fromArray.contains(" : true") & fromArray.contains(" : false");
    }
    
    @Test
    public void testRun() {
        try {
            Future<?> submit = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(pcNamesScanner);
            submit.get(20, TimeUnit.SECONDS);
        }
        catch (RuntimeException | ExecutionException | TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (InterruptedException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    public void testGetExecution() {
        String scannerExecution = pcNamesScanner.getExecution();
        Assert.assertTrue(scannerExecution.contains("<p>"));
    }
    
    @Test
    public void testGetPingResultStr() {
        String resultStr = pcNamesScanner.getPingResultStr();
        Assert.assertTrue(resultStr.contains("<p>"));
    }
    
    @Test
    public void testWriteLog() {
        File logFile = new File(FileNames.LASTNETSCAN_TXT);
        if (logFile.exists()) {
            Assert.assertTrue(logFile.delete());
        }
        String writeLogStr = pcNamesScanner.writeLog();
        Assert.assertEquals(logFile.getAbsolutePath(), writeLogStr);
        Assert.assertTrue(logFile.exists());
        logFile.deleteOnExit();
    }
    
    @Test
    public void testStartPlan() {
        InitProperties initProperties = InitProperties.getInstance(InitProperties.TEST);
        Properties toSetProps = new Properties();
        
        long nextStart = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY * 2);
        
        toSetProps.setProperty(PropertiesNames.LASTSCAN, String.valueOf(System.currentTimeMillis()));
        toSetProps.setProperty(PropertiesNames.NEXTSCAN, String.valueOf(nextStart));
        
        initProperties.setProps(toSetProps);
        initProperties = InitProperties.getInstance(InitProperties.FILE);
        initProperties.setProps(toSetProps);
        initProperties = InitProperties.getInstance(InitProperties.DB_LOCAL);
        initProperties.setProps(toSetProps);
        long dateLast = Long.parseLong(toSetProps.getProperty(PropertiesNames.LASTSCAN, "0"));
        String prefLastNext = MessageFormat
            .format("{0} last, {1} next", new Date(dateLast), new Date(dateLast + ConstantsFor.DELAY * 2));
        FileSystemWorker.appendObjectToFile(new File(FileNames.LASTNETSCAN_TXT), prefLastNext);
    }
    
    @Test
    public void scanTT() {
        try {
            scanAutoPC("tt", 3);
        }
        catch (UnknownFormatConversionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetStatistics() {
        String statistics = pcNamesScanner.getStatistics();
        System.out.println("statistics = " + statistics);
    }
    
    @Test
    public void scanPP() {
        scanAutoPC("pp", 5);
    }
    
    private static void checkBigDB() {
        try (Connection connection = DataConnectTo.getDefaultI().getDefaultConnection(name);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM velkompc order by 'TimeNow' desc LIMIT 1;");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                if (resultSet.first()) {
                    String timeNow = resultSet.getString("TimeNow");
                    Assert.assertTrue(checkDateFromDB(timeNow), timeNow);
                    break;
                }
            }
        }
        catch (SQLException | ParseException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}