package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Set;


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
        Assert.assertEquals(mysqlDataSource.getURL(), "jdbc:mysql://srv-inetstat.eatmeat.ru:3306/velkom", mysqlDataSource.getURL());
        try {
            Assert.assertTrue(mysqlDataSource.getConnection().isValid(15));
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetDefaultConnection() {
        try (Connection defaultConnection = mySqlLocalSRVInetStat.getDefaultConnection(ConstantsFor.DB_PCUSERAUTO_FULL)) {
            boolean defaultConnectionValid = defaultConnection.isValid(10);
            Assert.assertTrue(defaultConnectionValid);
            DatabaseMetaData metaData = defaultConnection.getMetaData();
            StringBuilder stringBuilder = new StringBuilder();
            try (ResultSet catalogs = metaData.getCatalogs()) {
                while(catalogs.next()){
                    stringBuilder.append(catalogs.getString(1)).append(" ");
                }
                Assert.assertTrue(stringBuilder.toString().contains(ConstantsFor.STR_VELKOM), stringBuilder.toString());
                System.out.println("stringBuilder = " + stringBuilder.toString());
            }
        }
        catch (SQLException e) {
            messageToUser.error(FileSystemWorker.error(this.getClass().getSimpleName() + ".testGetDefaultConnection", e));
        }
    }
    
    @Test
    public void testToString() {
        String toStr = mySqlLocalSRVInetStat.toString();
        System.out.println("toStr = " + toStr);
    }
    
    @Test
    public void testUploadCollection() {
        Path file = Paths.get(FileNames.BUILD_GRADLE);
        Set<String> stringSet = FileSystemWorker.readFileToEncodedSet(file, "UTF-8");
        int uploadFileTo = mySqlLocalSRVInetStat.uploadCollection(stringSet, "test.build_gradle");
        Assert.assertTrue(uploadFileTo > 0);
    }
}