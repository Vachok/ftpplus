package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.sql.*;
import java.util.Collections;


public class TesterDB65SQLTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(TesterDB65SQLTest.class.getSimpleName(), System
            .nanoTime());
    
    private DataConnectTo dataConnectTo;
    
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
    public void initDTC() {
        this.dataConnectTo = new TesterDB65SQL();
    }
    
    @Test
    public void testGetDataSource() {
        MysqlDataSource source = dataConnectTo.getDataSource();
        String urlInSource = source.getURL();
        if (UsefulUtilities.thisPC().toLowerCase().contains("home")) {
            Assert.assertEquals(urlInSource, "jdbc:mysql://srv-mysql.home:3306/");
        }
        else {
            Assert.assertEquals(urlInSource, "jdbc:mysql://srv-inetstat.eatmeat.ru:3306/velkom");
        }
    }
    
    @Test
    @Ignore
    public void testToLocalVM() {
        StringBuilder stringBuilder = new StringBuilder();
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.STR_VELKOM)) {
            Assert.assertEquals(connection.getMetaData().getURL(), "jdbc:mysql://srv-mysql.home:3306/velkom");
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM mysql.slow_log ORDER BY 'start_time' DESC LIMIT 10")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Timestamp timestampStart = resultSet.getTimestamp(1);
                        Time queryTime = resultSet.getTime("query_time");
                        int rowsSent = resultSet.getInt(5);
                        int rowsExam = resultSet.getInt(6);
                        String dbName = resultSet.getString(7);
                        String sqlQuery = resultSet.getString(11);
                        stringBuilder.append(timestampStart.toLocalDateTime()).append(" started long query: ").append(queryTime.toLocalTime()).append(" time, ")
                                .append("rows sent: ").append(rowsSent).append(" rows examined: ").append(rowsExam).append(" in DB: ").append(dbName).append(" sql: ")
                                .append(sqlQuery).append("\n");
                    }
                }
            }
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (InvokeIllegalException e) {
            if (UsefulUtilities.thisPC().toLowerCase().contains("do0")) {
                Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
            else {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
        }
        System.out.println("stringBuilder = " + stringBuilder.toString());
    }
    
    @Test
    public void testGetDefaultConnection() {
        try (Connection connection = dataConnectTo.getDefaultConnection("test.test")) {
            Assert.assertTrue(connection.isValid(5));
            String url = connection.getMetaData().getURL();
            System.out.println("url = " + url);
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void testCreateTable() {
        String tableName = "test.test" + System.currentTimeMillis();
        int test = dataConnectTo.createTable(tableName, Collections.emptyList());
        Assert.assertTrue(dataConnectTo.dropTable(tableName));
    }
}