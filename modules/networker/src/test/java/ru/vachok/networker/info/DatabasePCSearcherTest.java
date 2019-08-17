// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

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
 @see DatabasePCSearcher */
@SuppressWarnings("UnusedReturnValue")
public class DatabasePCSearcherTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(DatabasePCSearcher.class
        .getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private DatabaseInfo informationFactory = new DatabasePCSearcher();
    
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
    public void testGetInfoAbout$$COPY() {
        try {
            List<String> infoAbout = theInfoFromDBGetter("");
            Assert.assertTrue(infoAbout.size() > 0);
        }
        catch (UnknownHostException | UnknownFormatConversionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        try {
            List<String> infoAbout = theInfoFromDBGetter("test");
            Assert.assertTrue(infoAbout.size() > 0);
        }
        catch (UnknownHostException | UnknownFormatConversionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        try {
            List<String> infoAbout = theInfoFromDBGetter("do0213.eatmeat.ru");
            Assert.assertTrue(infoAbout.size() > 0);
            Assert.assertFalse(infoAbout.get(0).contains("Must be NOT NULL"));
            messageToUser.info(infoAbout);
        }
        catch (UnknownFormatConversionException | UnknownHostException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetInfoAbout() {
        String infoAbout = informationFactory.getInfoAbout("do0213");
        System.out.println("infoAbout = " + infoAbout);
    }
    
    @Test
    public void testToString() {
        String toStr = informationFactory.toString();
        Assert.assertTrue(toStr.contains("DatabasePCSearcher{"), toStr);
    }
    
    @Test
    public void testGetUserPCFromDB() {
        String searchingUser = informationFactory.getUserByPCNameFromDB("pivo");
        Assert.assertTrue(searchingUser.contains("do0045"), searchingUser);
    }
    
    @Test
    public void testGetPCUsersFromDB() {
        String searchingPCInfo = informationFactory.getCurrentPCUsers("do0213");
        throw new TODOException("14.08.2019 (0:01) ASSERT");
    }
    
    @Test
    public void userIsNotInDatabase() {
        String unknownUser = informationFactory.getUserByPCNameFromDB("j.doe");
        Assert.assertFalse(unknownUser.isEmpty());
    }
    
    private @NotNull List<String> theInfoFromDBGetter(@NotNull String thePcLoc) throws UnknownHostException, UnknownFormatConversionException {
        StringBuilder sqlQBuilder = new StringBuilder();
        
        if (thePcLoc.isEmpty()) {
            IllegalArgumentException argumentException = new IllegalArgumentException("Must be NOT NULL!");
            sqlQBuilder.append(argumentException.getMessage());
        }
        else if (new NameOrIPChecker(thePcLoc).resolveIP().isLinkLocalAddress()) {
            sqlQBuilder.append("select * from velkompc where NamePP like '%").append(thePcLoc).append("%'");
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