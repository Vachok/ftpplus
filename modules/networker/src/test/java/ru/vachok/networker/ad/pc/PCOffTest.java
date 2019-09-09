// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ConstantsNet;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;


/**
 @see PCOff */
@SuppressWarnings("UnusedReturnValue")
public class PCOffTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(PCOff.class
            .getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    private PCOff pcOff = new PCOff("do0213");
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testGetInfoAbout() {
        String infoAbout = pcOff.getInfoAbout("do0213");
        System.out.println("infoAbout = " + infoAbout);
    }
    
    @Test
    public void testToString() {
        String toStr = pcOff.toString();
        Assert.assertTrue(toStr.contains("PCOff["), toStr);
    }
    
    @Test(invocationCount = 3)
    public void testGetInfo() {
        this.pcOff = new PCOff("do0214");
        String factoryInfo = pcOff.getInfo();
        if (!NetScanService.isReach("do00214")) {
            Assert.assertTrue(factoryInfo.contains("Last online"), factoryInfo);
        }
        try {
            nullPcTest();
        }
        catch (RuntimeException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        badPcTest();
    }
    
    private void nullPcTest() {
        this.pcOff = new PCOff("");
        String factoryInfo = pcOff.getInfo();
    }
    
    private void badPcTest() {
        pcOff.setClassOption("do0");
        String offInfo = pcOff.getInfo();
        Assert.assertTrue(offInfo.contains("do0 not found"), offInfo);
    }
    
    private @NotNull List<String> theInfoFromDBGetter(@NotNull String thePcLoc) throws UnknownHostException, UnknownFormatConversionException {
        StringBuilder sqlQBuilder = new StringBuilder();
        
        if (thePcLoc.isEmpty()) {
            IllegalArgumentException argumentException = new IllegalArgumentException("Must be NOT NULL!");
            sqlQBuilder.append(argumentException.getMessage());
        }
        else if (new NameOrIPChecker(thePcLoc).resolveInetAddress().isLinkLocalAddress()) {
            sqlQBuilder.append(ConstantsFor.SQL_GET_VELKOMPC_NAMEPP).append(thePcLoc).append("%'");
            return dbGetter(thePcLoc, sqlQBuilder.toString());
        }
        return Collections.singletonList("ok");
    }
    
    private List<String> dbGetter(@NotNull String thePcLoc, final String sql) {
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return parseResultSet(resultSet, thePcLoc);
            }
        }
        catch (SQLException | IndexOutOfBoundsException | UnknownHostException e) {
            return Collections.singletonList(MessageFormat.format("DatabasePCSearcher.dbGetter: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    private @NotNull List<String> parseResultSet(@NotNull ResultSet resultSet, @NotNull String thePcLoc) throws SQLException, UnknownHostException {
        List<String> timeNowDatabaseFields = new ArrayList<>();
        List<Integer> integersOff = new ArrayList<>();
        while (resultSet.next()) {
            int onlineNow = resultSet.getInt(ConstantsNet.ONLINE_NOW);
            if (onlineNow == 1) {
                timeNowDatabaseFields.add(resultSet.getString(ConstantsFor.DBFIELD_TIMENOW));
            }
            else {
                integersOff.add(onlineNow);
            }
            StringBuilder stringBuilder = new StringBuilder();
            String namePP = new StringBuilder()
                    .append("<center><h2>").append(InetAddress.getByName(thePcLoc + ConstantsFor.DOMAIN_EATMEATRU)).append(" information.<br></h2>")
                    .append("<font color = \"silver\">OnLines = ").append(timeNowDatabaseFields.size())
                    .append(". Offline = ").append(integersOff.size()).append(". TOTAL: ")
                    .append(integersOff.size() + timeNowDatabaseFields.size()).toString();
            
            stringBuilder
                    .append(namePP)
                    .append(". <br>");
            messageToUser.info(stringBuilder.toString());
        }
        return timeNowDatabaseFields;
    }
}