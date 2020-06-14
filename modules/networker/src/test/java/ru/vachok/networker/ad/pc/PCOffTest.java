// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ConstantsNet;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UnknownFormatConversionException;


/**
 @see PCOff */
@SuppressWarnings("UnusedReturnValue")
public class PCOffTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(PCOff.class
            .getSimpleName(), System.nanoTime());

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PCOffTest.class.getSimpleName());

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
        String infoAbout = pcOff.getInfoAbout("test.eatmeat.ru");
        Assert.assertTrue(infoAbout.contains("Online"));
        Assert.assertTrue(infoAbout.contains("Offline"));
        Assert.assertTrue(infoAbout.contains("TOTAL"));
    }

    @Test
    public void testToString() {
        String toStr = pcOff.toString();
        Assert.assertEquals(toStr, "PCOff{pcName='test.eatmeat.ru', dbPCInfo=DBPCInfo{pcName='test.eatmeat.ru', sql='select * from velkompc where NamePP like ?'}}");
    }

    @Test
    public void testGetInfo() {
        String infoStr = pcOff.getInfo();
        Assert.assertTrue(infoStr.toLowerCase().contains("<a href=\"/ad?do0213\">"), infoStr);
    }

    private void nullPcTest() {
        this.pcOff = new PCOff("");
        String factoryInfo = pcOff.getInfo();
    }

    private void badPcTest() {
        pcOff.setClassOption("d00");
        String offInfo = pcOff.getInfo();
        Assert.assertTrue(offInfo.contains(DBPCHTMLInfo.DATABASES_NOT_REGISTRED), offInfo);
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

    @Test
    public void tryResolveA161() {
        try {
            pcOff.setClassOption("a161");
            String offInfo = pcOff.getInfo();
            Assert.fail();
        }
        catch (UnknownFormatConversionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
}