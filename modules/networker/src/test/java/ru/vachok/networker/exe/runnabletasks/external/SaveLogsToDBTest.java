// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks.external;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.concurrent.*;


/**
 @see SaveLogsToDB
 @since 14.06.2019 (16:55) */
public class SaveLogsToDBTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, SaveLogsToDBTest.class.getSimpleName());
    
    private SaveLogsToDB db = new SaveLogsToDB();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 4));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void testCall() {
        try {
            Future<String> submit = Executors.newSingleThreadExecutor().submit((Callable<String>) db);
            String dbCallable = submit.get(100, TimeUnit.SECONDS);
            Assert.assertTrue(dbCallable.contains("access.log"), dbCallable);
        }
        catch (TimeoutException | ExecutionException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (InterruptedException e) {
            messageToUser.error(MessageFormat
                .format("SaveLogsToDBTest.testCall {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
    }
    
    @Test
    public void testGetLastRecordID() {
        int id = db.getLastRecordID();
        Assert.assertTrue(id > 1000, id + " GetLastRecordID");
    }
    
    @Test
    public void testSaveAccessLogToDatabase() {
        Future<String> resOfSave = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit((Callable<String>) db);
        try {
            String saveLog = resOfSave.get(30, TimeUnit.SECONDS);
            Assert.assertTrue(saveLog.contains("Database updated: true"), saveLog);
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testSaveAccessLogToDatabaseWithTimeOut() {
        final int beforeID = db.getLastRecordID();
        String infoAbout = db.saveAccessLogToDatabaseWithTimeOut("60");
        Assert.assertTrue(infoAbout.contains("_access.log"), infoAbout);
        int afterID = db.getLastRecordID();
        Assert.assertTrue(beforeID < afterID, MessageFormat.format("{0} afterID-beforeID", afterID - beforeID));
    }
    
    @Test
    public void testToString() {
        String toStr = db.toString();
        Assert.assertTrue(toStr.contains("ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB["), toStr);
    }
    
    @Test
    public void testTestEquals() {
        boolean isEquals = this.db.equals(new SaveLogsToDB());
        Assert.assertFalse(isEquals);
    }
    
    @Test
    public void testTestHashCode() {
        Assert.assertTrue(db.hashCode() != new SaveLogsToDB().hashCode());
    }
    
    @Test
    public void testComment() {
        final String sql = "SELECT TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_NAME LIKE 'inetstats' AND TABLE_SCHEMA LIKE 'velkom';";
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection("velkom.inetstats");
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Assert.assertTrue(resultSet.getString(1).contains("rows "));
            }
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
}