// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.DBMessenger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;


/**
 @see DBMessenger
 @since 10.07.2019 (9:26) */
public class DBMessengerTest {
    
    
    private MessageToUser messageToUser = new DBMessenger(this.getClass().getSimpleName());
    
    private DataConnectTo dataConnectTo = new RegRuMysqlLoc();
    
    @Test
    public void sendMessage() {
        final String sql = "SELECT * FROM `ru_vachok_networker` ORDER BY `ru_vachok_networker`.`counter` DESC LIMIT 1";
        
        messageToUser.info(getClass().getSimpleName());
    
        Assert.assertTrue(checkMessageExistsInDatabase(sql));
    }
    
    private boolean checkMessageExistsInDatabase(String sql) {
        String dbName = ConstantsFor.DBNAME_WEBAPP;
    
        int executePS = 0;
    
        try (Connection c = dataConnectTo.getDefaultConnection(dbName);
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet resultSet = p.executeQuery();
        ) {
            while (resultSet.next()) {
                String bodyMsg = resultSet.getString("msgvalue");
                Assert.assertEquals(bodyMsg, getClass().getSimpleName(), resultSet.getString("pc"));
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
}