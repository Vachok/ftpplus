// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.message;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 @see DBMessenger
 @since 10.07.2019 (9:26) */
@SuppressWarnings("FieldCanBeLocal")
public class DBMessengerTest {
    
    
    private final String sql = "select * FROM velkom.last50logs";
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private DataConnectTo dataConnectTo = DataConnectTo.getDefaultI();
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, DBMessengerTest.class.getSimpleName());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void testToString() {
        String toStr = MessageToUser.getInstance(MessageToUser.DB, "test").toString();
        Assert.assertTrue(toStr.contains("DBMessenger{"), toStr);
    }
    
    @Test
    public void testWork() {
        new DBMessenger(DBMessengerTest.class.getSimpleName()).info("test", "test", "test");
        checkMessageExistsInDatabase();
    }
    
    private void checkMessageExistsInDatabase() {
        String dbName = ConstantsFor.DBBASENAME_U0466446_WEBAPP;
        
        long executePS;
        
        try (Connection c = dataConnectTo.getDefaultConnection("log.networker");
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet resultSet = p.executeQuery();
        ) {
            while (resultSet.next()) {
                executePS = resultSet.getLong("stamp");
                Assert.assertTrue(executePS > (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10)), MessageFormat
                    .format("{0} ({1})", executePS, new Date(executePS)));
            }
            Assert.assertFalse(resultSet.wasNull());
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat
                    .format("DBMessengerTest.checkMessageExistsInDatabase says: {0}. Parameters: \n[sql]: {1}", e.getMessage(), new TForms().fromArray(e)));
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        
    }
    
    private static long parseDate(String timeWhen) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(timeWhen).getTime();
        }
        catch (ParseException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        return 1;
    }
    
}