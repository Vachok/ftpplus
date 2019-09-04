// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.message;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;


/**
 @see DBMessenger
 @since 10.07.2019 (9:26) */
@SuppressWarnings("FieldCanBeLocal")
public class DBMessengerTest {
    
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.DB, this.getClass().getSimpleName());
    
    private final String sql = "SELECT * FROM `ru_vachok_networker` ORDER BY `ru_vachok_networker`.`timewhen` DESC LIMIT 1";
    
    private DataConnectTo dataConnectTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_WEBAPP);
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    private boolean checkMessageExistsInDatabase() {
        String dbName = ConstantsFor.DBBASENAME_U0466446_WEBAPP;
        
        int executePS = 0;
        
        try (Connection c = dataConnectTo.getDefaultConnection(dbName);
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet resultSet = p.executeQuery();
        ) {
            while (resultSet.next()) {
                String timeWhen = resultSet.getString("timewhen");
                long dbStamp = parseDate(timeWhen);
                Assert.assertTrue(dbStamp > (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)));
                executePS = resultSet.getInt("counter");
            }
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            messageToUser.error(MessageFormat
                .format("DBMessengerTest.checkMessageExistsInDatabase says: {0}. Parameters: \n[sql]: {1}", e.getMessage(), new TForms().fromArray(e)));
        }
        System.out.println("Records counter = " + executePS);
        return executePS > 0;
    }
    
    @Test
    public void testToString() {
        String toStr = MessageToUser.getInstance(MessageToUser.DB, "test").toString();
        Assert.assertTrue(toStr.contains("DBMessenger{"), toStr);
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