// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.ad.inet.AccessLogUSERTest;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ConstantsNet;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.io.File;
import java.sql.*;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;


/**
 @see DBPCHTMLInfo
 @since 18.08.2019 (23:41) */
public class DBPCHTMLInfoTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(AccessLogUSERTest.class.getSimpleName(), System.nanoTime());
    
    private DBPCHTMLInfo dbpchtmlInfo = new DBPCHTMLInfo();
    
    private String pcName = "do0213";
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        dbpchtmlInfo.setClassOption(pcName);
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @BeforeMethod
    public void initFields() {
        this.dbpchtmlInfo = new DBPCHTMLInfo();
        this.pcName = "do0213";
    }
    
    @Test
    public void testTestToString() {
        String do0213 = dbpchtmlInfo.toString();
        Assert.assertTrue(do0213.contains("DBPCInfo{"), do0213);
    }
    
    @Test
    public void testFillWebModel() {
        String fillWebModel = dbpchtmlInfo.fillWebModel();
        Assert.assertTrue(fillWebModel.contains("<a href="), fillWebModel);
        Assert.assertTrue(fillWebModel.contains("Last online"), fillWebModel);
    }
    
    @Test
    public void testFillAttribute() {
        String fillAttributeStr = dbpchtmlInfo.fillAttribute("no0027");
        Assert.assertTrue(fillAttributeStr.contains("Online = "), fillAttributeStr);
        Assert.assertTrue(fillAttributeStr.contains("Offline = "), fillAttributeStr);
        Assert.assertFalse(fillAttributeStr.contains("<br>"), fillAttributeStr);
    }
    
    @Test
    public void testSetClassOption() {
        dbpchtmlInfo.setClassOption("do0213");
        Assert.assertTrue(dbpchtmlInfo.toString().contains("DBPCInfo{pcName='do0213'"), dbpchtmlInfo.toString());
    }
    
    @Test
    public void testLastOnline() {
        dbpchtmlInfo.setClassOption("do0213");
        String lastOnline = dbpchtmlInfo.fillWebModel();
        Assert.assertTrue(lastOnline.contains("<a href=\"/ad?do0213\"><font color=\"red\">ikudryashov - do0213.eatmeat.ru. Last online: "), lastOnline);
    }
    
    @Test
    public void testCountOnOff() {
        String countOnOff = dbpchtmlInfo.fillAttribute("a242");
        Assert.assertTrue(countOnOff.contains("Online"), countOnOff);
        Assert.assertTrue(countOnOff.contains("Offline"), countOnOff);
        Assert.assertTrue(countOnOff.contains("TOTAL"), countOnOff);
    }
    
    @Test
    public void testFirstOnline() {
        String firstOnline = dbpchtmlInfo.getUserNameFromNonAutoDB();
        Assert.assertTrue(firstOnline.contains("Resolved:"), firstOnline);
    }
    
    @Test
    @Ignore
    public void startOfTimeOnResolve() {
        List<String> smallCounterPcNames = new ArrayList<>();
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection("velkom.pcuser")) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT pcname from pcuser WHERE timeon < '2019-11-04'")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String startName = resultSet.getString(1);
                        smallCounterPcNames.add(startName);
                    }
                    String message = AbstractForms.fromArray(smallCounterPcNames);
                    Assert.assertTrue(smallCounterPcNames.size() > 10, message);
                }
            }
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        for (String name : smallCounterPcNames) {
            this.pcName = name;
            FileSystemWorker.appendObjectToFile(new File("objects.set"), name);
//            setTimeOn();
        }
        
    }
    
    @Test
    @Ignore
    public void oldBobby() {
        this.pcName = "do0.eatmeat.ru";
        String onOff = countOnOff();
        System.out.println("onOff = " + onOff);
    }
    
    @Deprecated
    private @NotNull String countOnOff() {
        Thread.currentThread().checkAccess();
        Thread.currentThread().setPriority(1);
        Collection<Integer> onLine = new ArrayList<>();
        Collection<Integer> offLine = new ArrayList<>();
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.STR_VELKOM + "." + ConstantsFor.DB_PCUSERAUTO);
             PreparedStatement statement = connection.prepareStatement(ConstantsFor.SQL_GET_VELKOMPC_NAMEPP)) {
            statement.setString(1, String.format("%%%s%%", pcName));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int onlineNow = resultSet.getInt(ConstantsNet.ONLINE_NOW);
                    if (onlineNow == 1) {
                        onLine.add(1);
                    }
                    if (onlineNow == 0) {
                        offLine.add(0);
                    }
                }
                upPcUser(onLine.size(), offLine.size());
            }
            catch (RuntimeException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
            }
        }
        catch (SQLException | RuntimeException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        return new DBPCHTMLInfo().htmlOnOffCreate(onLine.size(), offLine.size());
    }
    
    @Deprecated
    private void upPcUser(int on, int off) {
        String wherePcName = " WHERE `pcName` like '";
        final String sqlOn = String.format("UPDATE `velkom`.`pcuser` SET `On`= %d%s%s%%'", on, wherePcName, pcName);
        final String sqlOff = String.format("UPDATE `velkom`.`pcuser` SET `Off`= %d%s%s%%'", off, wherePcName, pcName);
        final String sqlTotal = String.format("UPDATE `velkom`.`pcuser` SET `Total`= %d%s%s%%'", on + off, wherePcName, pcName);
        
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.TESTING).getDefaultConnection(ConstantsFor.DB_VELKOMPCUSER);
             PreparedStatement psOn = connection.prepareStatement(sqlOn);
             PreparedStatement psOff = connection.prepareStatement(sqlOff);
             PreparedStatement psTotal = connection.prepareStatement(sqlTotal);
        ) {
            psOn.executeUpdate();
            psOff.executeUpdate();
            psTotal.executeUpdate();
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    private void setTimeOn() {
        this.pcName = new NameOrIPChecker(pcName).resolveInetAddress().getHostAddress();
        JsonObject jsonObject = new JsonObject();
        jsonObject.set("start", System.nanoTime());
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection("velkom.velkompc")) {
            String sql = "SELECT * from velkompc WHERE AddressPP like ? and OnlineNow = 1 order by idrec desc limit 1";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, String.format("%s%%", pcName));
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    jsonObject.set("setTimeOn", preparedStatement.toString().split(": ")[1]);
                    while (resultSet.next()) {
                        this.pcName = resultSet.getString("NamePP");
                        jsonObject.set("setTimeOn", pcName);
                        jsonObject.set("idrec", resultSet.getInt("idrec"));
                        jsonObject.set("laston", resultSet.getTimestamp("TimeNow").getTime());
                    }
                }
            }
            setOnTime(jsonObject);
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    private void setOnTime(@NotNull JsonObject jsonObject) {
        long longStamp = jsonObject.getLong("laston", MyCalen.getLongFromDate(7, 1, 1984, 2, 0));
        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(longStamp / 1000, 0, ZoneOffset.ofHours(3));
        Timestamp timestamp = Timestamp.valueOf(localDateTime);
        Assert.assertTrue(timestamp.getTime() == longStamp);
        
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection("velkom.pcuser")) {
            String sql = "SELECT * from velkompc WHERE TimeNow > ? and NamePP like ? AND onlinenow=1 order by idrec ASC limit 1";
            try (PreparedStatement preparedStatementFirst = connection.prepareStatement(sql)) {
                preparedStatementFirst.setTimestamp(1, timestamp);
                preparedStatementFirst.setString(2, String.format("%s%%", pcName));
                try (ResultSet resultSet = preparedStatementFirst.executeQuery()) {
                    jsonObject.set("setOnTime", preparedStatementFirst.toString().split(": ")[1]);
                    while (resultSet.next()) {
                        jsonObject.add("on", resultSet.getTimestamp("TimeNow").getTime());
                        jsonObject.add("pc", pcName);
                    }
                }
            }
            finaliz(jsonObject);
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    private void finaliz(@NotNull JsonObject jsonObject) {
        long aLong = jsonObject.getLong("on", MyCalen.getLongFromDate(7, 1, 1984, 2, 0));
        
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection("velkom.pcuser")) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE `velkom`.`pcuser` SET `timeon`=? WHERE  pcname like ?")) {
                preparedStatement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.ofEpochSecond(aLong / 1000, 0, ZoneOffset.ofHours(3))));
                preparedStatement.setString(2, String.format("%s%%", pcName));
                preparedStatement.executeUpdate();
                jsonObject.set("finaliz", preparedStatement.toString().split(": ")[1]);
                jsonObject.set("stop", System.nanoTime());
                File append = new File("objects.set");
                FileSystemWorker.appendObjectToFile(append, jsonObject);
                long timeDiff = jsonObject.getLong("stop", System.nanoTime()) - jsonObject.getLong("start", System.nanoTime());
                System.out.println(pcName + " = " + FileSystemWorker.appendObjectToFile(append, MessageFormat.format("{0} execution nanoseconds", timeDiff)));
            }
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
}