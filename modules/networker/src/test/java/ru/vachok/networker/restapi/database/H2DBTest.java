package ru.vachok.networker.restapi.database;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.DatabaseCleanerFromDuplicatesTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 @see H2DB
 @since 01.11.2019 (9:40) */
public class H2DBTest {
    
    
    private H2DB h2DB;
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(DatabaseCleanerFromDuplicatesTest.class
            .getSimpleName(), System.nanoTime());
    
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
    public void initH2() {
        this.h2DB = new H2DB();
    }
    
    @Test
    public void testCreateConnect() {
        try {
            Class.forName("org.h2.Driver");
        }
        catch (ClassNotFoundException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.H2DB).getDefaultConnection("test.test")) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE `test` (\n" +
                    "\t`idrec` MEDIUMINT(11) UNSIGNED NOT NULL AUTO_INCREMENT,\n" +
                    "\t`stamp` BIGINT(13) NOT NULL DEFAULT '442278000000',\n" +
                    "\t`counter` INT(11) NOT NULL DEFAULT '1',\n" +
                    "\tPRIMARY KEY (`idrec`));")) {
                preparedStatement.executeUpdate();
                try (PreparedStatement preparedStatement1 = connection
                        .prepareStatement(String.format("insert into test (stamp, counter) values (%d, 2)", System.currentTimeMillis()))) {
                    preparedStatement1.executeUpdate();
                    try (PreparedStatement preparedStatement2 = connection.prepareStatement("select * from test")) {
                        try (ResultSet resultSet = preparedStatement2.executeQuery()) {
                            while (resultSet.next()) {
                                System.out.println("resultSet = " + resultSet.getString(1) + " " + resultSet.getString(2));
                            }
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void createTablesFromExport() {
        final String sql = FileSystemWorker.readRawFile(this.getClass().getResource("/log.createtable.sql").getFile());
        
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.H2DB).getDefaultConnection("log.networker");
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int execUpd = preparedStatement.executeUpdate();
            Assert.assertTrue(execUpd == 0, String.valueOf(execUpd));
            nextStep();
        }
        catch (SQLException e) {
            String messageErr = e.getMessage() + "\n" + AbstractForms.exceptionNetworker(e.getStackTrace());
            Assert.assertNull(e, messageErr);
        }
    }
    
    @Test
    public void testGetDataSource() {
        try {
            h2DB.getDataSource();
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void testUploadCollection() {
        try {
            h2DB.uploadCollection(Collections.singleton("test"), "test");
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void testDropTable() {
        try {
            h2DB.dropTable("test");
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void testCreateTable() {
        List<String> colList = new ArrayList<>();
        colList.add("`upstring` VARCHAR(100) NOT NULL DEFAULT 'not set'");
        int tableCreate = h2DB.createTable("test.test", colList);
        Assert.assertTrue(tableCreate == 0);
    }
    
    @Test
    public void testTestToString() {
        String toStr = h2DB.toString();
        Assert.assertEquals(toStr, "H2DB{}");
    }
    
    @Test
    public void testGetDefaultConnection() {
        try (Connection connection = h2DB.getDefaultConnection("test.test")) {
            Assert.assertTrue(connection.isValid(5));
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    private void nextStep() {
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.H2DB).getDefaultConnection("log.networker")) {
            try (PreparedStatement preparedStatementIns = connection
                    .prepareStatement("insert into networker (idrec, upstring,pc, stamp, classname) values (1,'UP!','mypc', 4400, ?)")) {
                preparedStatementIns.setString(1, this.getClass().getSimpleName());
                preparedStatementIns.executeUpdate();
                try (PreparedStatement preparedStatementGet = connection.prepareStatement("select * from networker");
                     ResultSet resultSet = preparedStatementGet.executeQuery()) {
                    while (resultSet.next()) {
                        Assert.assertEquals(resultSet.getLong("stamp"), 4400);
                        Assert.assertEquals(resultSet.getString("classname"), this.getClass().getSimpleName());
                    }
                }
            }
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
}
