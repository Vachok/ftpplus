package ru.vachok.networker.data;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.sql.*;
import java.util.*;


/**
 @see DatabaseCleanerFromDuplicates
 @since 25.10.2019 (21:45) */
public class DatabaseCleanerFromDuplicatesTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(DatabaseCleanerFromDuplicatesTest.class
        .getSimpleName(), System.nanoTime());
    
    private DatabaseCleanerFromDuplicates databaseCleanerFromDuplicates;
    
    @BeforeMethod
    public void initClean() {
        this.databaseCleanerFromDuplicates = new DatabaseCleanerFromDuplicates();
    }
    
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
    public void testUploadCollection() {
        try {
            databaseCleanerFromDuplicates.uploadCollection(Collections.singleton("test"), "test");
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void testDropTable() {
        try {
            databaseCleanerFromDuplicates.dropTable("test");
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void testGetDataSource() {
        try {
            MysqlDataSource source = databaseCleanerFromDuplicates.getDataSource();
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    @Ignore
    public void logicMaking() {
        List<Timestamp> tsList = new ArrayList<>();
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.TESTING).getDefaultConnection("velkom")) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT distinct whenQueried FROM pcuserauto;")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        tsList.add(resultSet.getTimestamp("whenQueried"));
                    }
                    Assert.assertTrue(tsList.size() > 1, "NO DISTINCT whenQueried!");
                    getGoodIDs(tsList);
                }
            }
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    private void getGoodIDs(@NotNull List<Timestamp> list) {
        Queue<Integer> goodIDs = new LinkedList<>();
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.TESTING).getDefaultConnection("velkom")) {
            for (Timestamp timestamp : list) {
                try (PreparedStatement preparedStatement = connection.prepareStatement("select * from pcuserauto where whenQueried like ?")) {
                    preparedStatement.setTimestamp(1, timestamp);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            goodIDs.add(resultSet.getInt("idRec"));
                        }
                    }
                }
                catch (RuntimeException e) {
                    Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
                }
            }
            Assert.assertTrue(goodIDs.size() > 1, "No IDs collected...");
            System.out.println("goodIDs = " + goodIDs.size());
            delOther(goodIDs);
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    private void delOther(Queue<Integer> ds) {
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.TESTING).getDefaultConnection("velkom")) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("select * from pcuserauto");
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (!ds.isEmpty()) {
                    int recId = ds.remove();
                    
                }
            }
            
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
}