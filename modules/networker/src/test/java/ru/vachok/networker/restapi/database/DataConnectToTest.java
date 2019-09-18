// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.sql.*;


/**
 @see DataConnectTo
 @since 13.08.2019 (0:16) */
public class DataConnectToTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(DataConnectToTest.class.getSimpleName(), System.nanoTime());
    
    private DataConnectTo dataConnectTo = ru.vachok.networker.restapi.database.DataConnectTo.getInstance(ConstantsFor.DBBASENAME_U0466446_TESTING);
    
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
    public void testSetSavepoint() {
        try {
            dataConnectTo.setSavepoint(new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_TESTING));
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetDataSource() {
        MysqlDataSource dSource = dataConnectTo.getDataSource();
        String url = dSource.getURL();
        Assert.assertTrue(url.contains("server202.hosting.reg.ru:3306/u0466446_testing"), url);
    }
    
    @Test
    public void testGetDefaultConnection() {
        Connection connection = ru.vachok.networker.restapi.database.DataConnectTo.getInstance(ru.vachok.networker.restapi.database.DataConnectTo.LOC_INETSTAT)
                .getDefaultConnection(ConstantsFor.DB_SEARCH);
        try {
            DatabaseMetaData connectionMetaData = connection.getMetaData();
            String metaDataURL = connectionMetaData.getURL();
            Assert.assertEquals(metaDataURL, "jdbc:mysql://srv-inetstat.eatmeat.ru:3306/search");
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetSavepoint() {
        try {
            dataConnectTo.getSavepoint(new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_TESTING));
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}