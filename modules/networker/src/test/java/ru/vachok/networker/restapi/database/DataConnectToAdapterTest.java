package ru.vachok.networker.restapi.database;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;

import java.sql.Connection;
import java.sql.SQLException;


/**
 @see DataConnectToAdapter
 @since 16.07.2019 (17:09) */
public class DataConnectToAdapterTest {
    
    
    private String dbName = ConstantsFor.DBBASENAME_U0466446_TESTING;
    
    @Test
    public void testGetRegRuMysqlLibConnection() {
        try (Connection connection = new RegRuMysql().getDefaultConnection(dbName)) {
            boolean connectionValid = connection.isValid(5);
            Assert.assertTrue(connectionValid);
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetI() {
    }
    
    @Test
    public void testSetSavepoint() {
    }
    
    @Test
    public void testGetDataSource() {
    }
    
    @Test
    public void testGetSavepoint() {
    }
}