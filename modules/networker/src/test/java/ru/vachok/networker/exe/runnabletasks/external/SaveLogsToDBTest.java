// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks.external;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.concurrent.*;


/**
 @see SaveLogsToDB
 @since 14.06.2019 (16:55) */
@SuppressWarnings("ALL")
public class SaveLogsToDBTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private SaveLogsToDB db = new SaveLogsToDB();
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
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
    public void testGetIDDifferenceWhileAppRunning() {
        int difference = db.getIDDifferenceWhileAppRunning();
        Assert.assertTrue(difference > 0, difference + " GetIDDifferenceWhileAppRunning");
    }
    
    @Test
    public void testGetLastRecordID() {
        int id = db.getLastRecordID();
        Assert.assertTrue(id > 1000, id + " GetLastRecordID");
    }
    
    @Test
    public void testSaveAccessLogToDatabase() {
        String saveLog = db.saveAccessLogToDatabase();
        Assert.assertTrue(saveLog.contains("Database updated: true"), saveLog);
    }
    
    @Test
    public void testSaveAccessLogToDatabaseWithTimeOut() {
        String infoAbout = db.saveAccessLogToDatabaseWithTimeOut("70");
        Assert.assertTrue(infoAbout.contains("accessLogUsers = 0"), infoAbout);
    }
    
    @Test
    public void testTestToString1() {
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
//    @Ignore
    public void syncDB() {
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.DBUSER_NETWORK);
        String toStr = dataConnectTo.toString();
        Assert.assertEquals(toStr, new StringBuilder().append("RegRuMysqlLoc[\n").append("dbName = 'u0466446_velkom'\n").append("]").toString());
        try (Connection connection = dataConnectTo.getDataSource().getConnection();
    
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `velkompc`");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            int idInLocalDB = getLastID();
            System.out.println("idInLocalDB = " + idInLocalDB);
            while (resultSet.next()) {
                int idvelkompc = resultSet.getInt("idvelkompc");
                if (idvelkompc > idInLocalDB) {
                    writeLocalOverRemote(idvelkompc, resultSet);
                }
            }
        
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    private int getLastID() {
        int retInt = 20000000;
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
        MysqlDataSource source = dataConnectTo.getDataSource();
        source.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        try (Connection connection = source.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("select idvelkompc from velkompc ORDER BY idvelkompc DESC LIMIT 1");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                if (resultSet.last()) {
                    retInt = resultSet.getInt("idvelkompc");
                }
            }
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        return retInt;
    }
    
    private void writeLocalOverRemote(int idvelkompc, @NotNull ResultSet resultSet) {
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
        
        try (Connection localConnection = dataConnectTo.getDataSource().getConnection();
             PreparedStatement psmtLocal = localConnection
                 .prepareStatement("INSERT INTO `u0466446_velkom`.`velkompc` (`idvelkompc`, `NamePP`, `AddressPP`, `SegmentPP`, `instr`, `OnlineNow`, `userName`, `TimeNow`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            psmtLocal.setInt(1, idvelkompc);
            psmtLocal.setString(2, resultSet.getString("NamePP"));
            psmtLocal.setString(3, resultSet.getString("AddressPP"));
            psmtLocal.setString(4, resultSet.getString("SegmentPP"));
            psmtLocal.setString(5, resultSet.getString("instr"));
            psmtLocal.setInt(6, resultSet.getInt("OnlineNow"));
            psmtLocal.setString(7, resultSet.getString("userName"));
            psmtLocal.setString(8, resultSet.getString("TimeNow"));
            psmtLocal.executeUpdate();
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    @Ignore
    public void writeLocalDBFromFile() {
        try (InputStream inputStream = new FileInputStream("velkompc.table");
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            bufferedReader.lines().forEach(x->writeToLocalDB(x.split(",")));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    private void writeToLocalDB(@NotNull String[] arrFromStringInFileDumpedFromRemote) {
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
        try (Connection localConnection = dataConnectTo.getDataSource().getConnection();
             PreparedStatement psmtLocal = localConnection
                 .prepareStatement("INSERT INTO `u0466446_velkom`.`velkompc` (`idvelkompc`, `NamePP`, `AddressPP`, `SegmentPP`, `OnlineNow`, `userName`, `TimeNow`) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            psmtLocal.setInt(1, Integer.parseInt(arrFromStringInFileDumpedFromRemote[0]));
            psmtLocal.setString(2, arrFromStringInFileDumpedFromRemote[1]);
            psmtLocal.setString(3, arrFromStringInFileDumpedFromRemote[2]);
            psmtLocal.setString(4, arrFromStringInFileDumpedFromRemote[3]);
            
            psmtLocal.setInt(5, Integer.parseInt(arrFromStringInFileDumpedFromRemote[5]));
            psmtLocal.setString(6, arrFromStringInFileDumpedFromRemote[6]);
            psmtLocal.setString(7, arrFromStringInFileDumpedFromRemote[7]);
            psmtLocal.executeUpdate();
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    private void writeToLocalFile(int idvelkompc, @NotNull ResultSet resultSet) {
        try (OutputStream outputStream = new FileOutputStream("velkompc.table", true);
             PrintStream psmtLocal = new PrintStream(outputStream, true)) {
            psmtLocal.printf("%s,", idvelkompc);
            psmtLocal.printf("%s,", resultSet.getString("NamePP"));
            psmtLocal.printf("%s,", resultSet.getString("AddressPP"));
            psmtLocal.printf("%s,", resultSet.getString("SegmentPP"));
            psmtLocal.printf("%s,", resultSet.getString("instr"));
            psmtLocal.printf("%s,", resultSet.getInt("OnlineNow"));
            psmtLocal.printf("%s,", resultSet.getString("userName"));
            psmtLocal.printf("%s,", resultSet.getString("TimeNow"));
            psmtLocal.println();
        }
        catch (IOException | SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}