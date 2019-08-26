package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.NotNull;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.componentsrepo.data.NetKeeper;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 @see PcNamesScanner */
public class PcNamesScannerTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
    private PcNamesScanner pcNamesScanner = new PcNamesScanner();
    
    private NetScanCtr netScanCtr = new NetScanCtr(new PcNamesScanner());
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        this.pcNamesScanner.setClassOption(netScanCtr);
        this.pcNamesScanner.setThePc("do0001");
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testTestToString() {
        String toStr = pcNamesScanner.toString();
        Assert.assertTrue(toStr.contains("PcNamesScanner["), toStr);
        System.out.println("toStr = " + toStr);
    }
    
    @Test
    public void testFillAttribute() {
        pcNamesScanner.fillAttribute("do0213");
    }
    
    @Test
    public void testFillWebModel() {
        this.netScanCtr.netScan(new MockHttpServletRequest(), new MockHttpServletResponse(), new ExtendedModelMap());
        String webM = pcNamesScanner.fillWebModel();
        Assert.assertTrue(webM.equals("<p>"));
    }
    
    @Test
    public void testSetClassOption() {
        String toStr = pcNamesScanner.toString();
        Assert.assertTrue(toStr.contains("model = true"), toStr);
    }
    
    @Test
    public void scanDO() {
        scanAutoPC("do");
    }
    
    private void scanAutoPC(String testPrefix) {
        final long startMethTime = System.currentTimeMillis();
        String pcsString;
        Collection<String> autoPcNames = new ArrayList<>(getCycleNames(testPrefix));
        Assert.assertEquals(autoPcNames.size(), 9);
    
        for (String pcName : autoPcNames) {
            InformationFactory informationFactory = InformationFactory.getInstance(pcName);
            informationFactory.getInfo();
        }
        prefixToMap(testPrefix);
        pcsString = PCInfo.writeToDB();
        Assert.assertTrue(checkDB());
        messageToUser.info(pcsString);
        
        String elapsedTime = "<b>Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethTime) + " sec.</b> " + LocalTime.now();
        NetKeeper.getPcNamesForSendToDatabase().add(elapsedTime);
        
        String pcNamesSet = new TForms().fromArray(NetKeeper.getPcNamesForSendToDatabase());
        Assert.assertFalse(pcNamesSet.isEmpty());
        Assert.assertFalse(pcNamesSet.contains("ruonline"), pcNamesSet);
        Assert.assertTrue(pcNamesSet.contains("10."), pcNamesSet);
    }
    
    private boolean checkDB() {
        boolean retBool = false;
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `velkompc_TimeNow`");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                if (resultSet.first()) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Date parseDate = format.parse(resultSet.getString("TimeNow"));
                    System.out.println("parseDate = " + parseDate);
                    retBool = parseDate.getTime() > (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5));
                    break;
                }
            }
        }
        catch (SQLException | ParseException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            retBool = false;
        }
        return retBool;
    }
    
    private @NotNull List<String> getCycleNames(String namePCPrefix) {
        if (namePCPrefix == null) {
            namePCPrefix = "pp";
        }
        int inDex = 10;
        String nameCount;
        List<String> list = new ArrayList<>();
        int pcNum = 0;
        for (int i = 1; i < inDex; i++) {
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
    public void scanTT() {
        try {
            scanAutoPC("tt");
        }
        catch (UnknownFormatConversionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void scanPP() {
        scanAutoPC("pp");
    }
    
}