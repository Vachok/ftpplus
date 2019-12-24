package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.NotNull;
import org.springframework.ui.ExtendedModelMap;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ad.user.UserInfo;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;


/**
 @see PcNamesScanner */
public class PcNamesScannerTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(PcNamesScannerTest.class.getSimpleName(), System
        .nanoTime());

    private static final PcNamesScanner PC_SCANNER = new PcNamesScanner();

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PcNamesScanner.class.getSimpleName());

    private NetScanCtr netScanCtr;

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        try {
            Files.deleteIfExists(new File(FileNames.SCAN_TMP).toPath());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        this.netScanCtr = new NetScanCtr(new PcNamesScannerWorks());
        this.netScanCtr.setModel(new ExtendedModelMap());
    }

    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }

    @BeforeMethod
    public void initScan() {
        this.netScanCtr = new NetScanCtr(PC_SCANNER);
    }

    @Test
    public void testToString() {
        String toStr = PC_SCANNER.toString();
        Assert.assertTrue(toStr.contains("startClassTime"), toStr);
        Assert.assertTrue(toStr.contains("lastScanStamp"), toStr);
        Assert.assertTrue(toStr.contains("thePc"), toStr);
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
            scanName(pcName);
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

    private @NotNull String scanName(String pcName) {
        InformationFactory informationFactory = InformationFactory.getInstance(pcName);
        String pcNameInfo = informationFactory.getInfo();
        return MessageFormat.format("{0} parameter. Result:  {1}", pcName, pcNameInfo);
    }

    private static void prefixToMap(String prefixPcName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<h4>");
        stringBuilder.append(prefixPcName);
        stringBuilder.append("     ");
        stringBuilder.append(NetKeeper.getPcNamesForSendToDatabase().size());
        stringBuilder.append("</h4>");
        String netMapKey = stringBuilder.toString();
    }

    @Test
    public void scanA() {
        String a224Scan = scanName("a224");
        Assert.assertTrue(a224Scan.contains("a224"), a224Scan);
        boolean isUser = a224Scan.contains("ialekseeva") || a224Scan.contains("n.kolodyazhnyj");
        Assert.assertTrue(isUser, a224Scan);
    }

    @Test
    public void testFileScanTMPCreate() {
        File file = new File(FileNames.SCAN_TMP);
        boolean isMethodOk = PcNamesScanner.fileScanTMPCreate(true);
        Assert.assertTrue(file.exists());
        Assert.assertTrue(isMethodOk);
        isMethodOk = PcNamesScanner.fileScanTMPCreate(false);
        Assert.assertFalse(file.exists());
        Assert.assertTrue(isMethodOk);
    }

    private static void checkWeekDB() {
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.TESTING).getDefaultConnection("velkom.pcuserauto");
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM pcuserauto order by idrec desc LIMIT 1");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                if (resultSet.first()) {
                    Assert.assertTrue(checkDateFromDB(resultSet.getString(ConstantsFor.DB_FIELD_WHENQUERIED)));
                    break;
                }
            }
        }
        catch (SQLException | ParseException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }

    private static boolean checkDateFromDB(String timeNow) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale.forLanguageTag("ru, RU"));
        Date parseDate = format.parse(timeNow);
        return parseDate.getTime() > (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(72));
    }

    @Test
    public void testGetMonitoringRunnable() {
        Runnable runnable = PC_SCANNER.getMonitoringRunnable();
        Assert.assertEquals(runnable, PC_SCANNER);
        String runToStr = runnable.toString();
        Assert.assertTrue(runToStr.contains("{\"startClassTime\":"), runToStr);
    }

    @Test
    public void testOnePrefixSET() {
        NetKeeper.getPcNamesForSendToDatabase().clear();
        Set<String> notdScanned = PC_SCANNER.onePrefixSET("dotd");
        Assert.assertTrue(notdScanned.size() > 3);
        String setStr = AbstractForms.fromArray(notdScanned);
        Assert.assertTrue(setStr.contains(ConstantsFor.ELAPSED), setStr);
        checkBigDB();
    }

    @Test
    public void testRun() {
        try {
            Future<?> submit = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(PC_SCANNER);
            Assert.assertNull(submit.get(20, TimeUnit.SECONDS));
        }
        catch (RuntimeException | ExecutionException | TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        catch (InterruptedException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void scanTT() {
        try {
            scanAutoPC("tt", 3);
        }
        catch (UnknownFormatConversionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }

    @Test
    public void testGetExecution() {
        String scannerExecution = PC_SCANNER.getExecution();
        Assert.assertTrue(scannerExecution.contains("<p>"));
    }

    @Test
    public void testGetPingResultStr() {
        String resultStr = PC_SCANNER.getPingResultStr();
        Assert.assertTrue(resultStr.contains("<p>"));
    }

    @Test
    public void testWriteLog() {
        File logFile = new File(FileNames.LASTNETSCAN_TXT);
        if (logFile.exists()) {
            Assert.assertTrue(logFile.delete());
        }
        String writeLogStr = PC_SCANNER.writeLog();
        Assert.assertEquals(logFile.getAbsolutePath(), writeLogStr);
        Assert.assertTrue(logFile.exists());
        logFile.deleteOnExit();
    }

    @Test
    public void testStartPlan() {
        InitProperties initProperties = InitProperties.getInstance(InitProperties.TEST);
        Properties toSetProps = new Properties();

        long nextStart = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY * 2);
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

    private static void checkBigDB() {
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_VELKOMVELKOMPC)) {
            String urlStr = connection.getMetaData().getURL();
            Assert.assertTrue(urlStr.contains(OtherKnownDevices.SRV_INETSTAT), urlStr);
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM velkompc order by idrec desc LIMIT 1")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        if (resultSet.first()) {
                            String timeNow = resultSet.getString(ConstantsFor.DBFIELD_TIMENOW);
                            Assert.assertTrue(checkDateFromDB(timeNow), timeNow);
                            break;
                        }
                    }
                }
            }
        }
        catch (SQLException | ParseException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }

    @Test
    public void testGetStatistics() {
        String statistics = PC_SCANNER.getStatistics();
        System.out.println("statistics = " + statistics);
    }

    @Test
    public void scanPP() {
        scanAutoPC("pp", 5);
    }

    private static boolean checkMap() {
        ConcurrentNavigableMap<String, Boolean> htmlLinks = NetKeeper.getUsersScanWebModelMapWithHTMLLinks();
        String fromArray = AbstractForms.fromArray(htmlLinks);
        return fromArray.contains(" : true") & fromArray.contains(" : false");
    }
}