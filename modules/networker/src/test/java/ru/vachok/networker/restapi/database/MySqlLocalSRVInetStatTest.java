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
 @see MySqlLocalSRVInetStat
 @since 04.09.2019 (17:07) */
public class MySqlLocalSRVInetStatTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(MySqlLocalSRVInetStat.class
            .getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    private MySqlLocalSRVInetStat mySqlLocalSRVInetStat;
    
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
        this.mySqlLocalSRVInetStat = new MySqlLocalSRVInetStat();
    }
    
    @Test
    public void testGetDataSource() {
        MysqlDataSource mysqlDataSource = mySqlLocalSRVInetStat.getDataSource();
        Assert.assertEquals(mysqlDataSource.getURL(),"jdbc:mysql://srv-inetstat.eatmeat.ru:3306/", mysqlDataSource.getURL());
    }
    
    @Test
    public void testGetDefaultConnection() {
        try (Connection defaultConnection = mySqlLocalSRVInetStat.getDefaultConnection("velkom")) {
            boolean defaultConnectionValid = defaultConnection.isValid(10);
            Assert.assertTrue(defaultConnectionValid);
            DatabaseMetaData metaData = defaultConnection.getMetaData();
            StringBuilder stringBuilder = new StringBuilder();
            try (ResultSet catalogs = metaData.getCatalogs()) {
                while(catalogs.next()){
                    stringBuilder.append(catalogs.getString(1)).append(" ");
                }
                Assert.assertTrue(stringBuilder.toString().contains("velkom"), stringBuilder.toString());
            }
        }
        catch (SQLException e) {
            messageToUser.error(FileSystemWorker.error(this.getClass().getSimpleName() + ".testGetDefaultConnection", e));
        }
    }
    
    @Test
    public void testTestToString() {
        String toStr = mySqlLocalSRVInetStat.toString();
        System.out.println("toStr = " + toStr);
    }
}