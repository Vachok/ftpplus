package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.*;


/**
 @see MySqlInetStat
 @since 04.09.2019 (17:07) */
public class MySqlInetStatTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(MySqlInetStat.class
            .getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    private MySqlInetStat mySqlInetStat;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @BeforeMethod
    public void initMySql() {
        this.mySqlInetStat = new MySqlInetStat();
    }
    
    @Test
    public void testGetDataSource() {
        MysqlDataSource mysqlDataSource = mySqlInetStat.getDataSource();
        Assert.assertEquals(mysqlDataSource.getURL(),"jdbc:mysql://srv-inetstat.eatmeat.ru:3306/", mysqlDataSource.getURL());
    }
    
    @Test
    public void testGetDefaultConnection() {
        try (Connection defaultConnection = mySqlInetStat.getDefaultConnection("velkom")) {
            boolean defaultConnectionValid = defaultConnection.isValid(10);
            Assert.assertTrue(defaultConnectionValid);
            DatabaseMetaData metaData = defaultConnection.getMetaData();
            try (ResultSet catalogs = metaData.getClientInfoProperties()) {
                while(catalogs.next()){
                    System.out.println("catalogs = " + catalogs.getString(1));
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(FileSystemWorker.error(this.getClass().getSimpleName() + ".testGetDefaultConnection", e));
        }
    }
    
    @Test
    public void testTestToString() {
        String toStr = mySqlInetStat.toString();
        System.out.println("toStr = " + toStr);
    }
}