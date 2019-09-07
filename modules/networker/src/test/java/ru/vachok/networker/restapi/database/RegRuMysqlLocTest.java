// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.PropertiesNames;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @see RegRuMysqlLoc
 @since 14.07.2019 (12:34) */
public class RegRuMysqlLocTest {
    
    
    private DataConnectTo dataConTo;
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @BeforeMethod
    public void initDcT() {
        this.dataConTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_TESTING);
    }
    
    @Test
    public void testGetDefaultConnection() {
        try (Connection connection = dataConTo.getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_TESTING);
             PreparedStatement p = connection.prepareStatement("INSERT INTO `u0466446_testing`.`fake` (`Rec`) VALUES (?)")) {
            p.setString(1, LocalDateTime.now().toString());
            Assert.assertTrue(p.executeUpdate() > 0);
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }catch (InvokeEmptyMethodException e){
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testToString() {
        
        try{
            System.out.println(dataConTo.toString());
        }catch (ExceptionInInitializerError e){
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetDataSource() {
        MysqlDataSource source = dataConTo.getDataSource();
        Assert.assertEquals("jdbc:mysql://server202.hosting.reg.ru:3306/u0466446_testing", source.getURL());
    }
    
    @Test
    @Ignore
    public void testTestSyncDB() {
        Runnable runnable = ()->DataConnectTo.syncDB(ConstantsFor.TABLE_VELKOMPC);
        Future<?> submit = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(runnable);
        try {
            submit.get(20, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    @Ignore
    public void syncWithRunnableRun() {
        DataConnectTo.syncDB(ConstantsFor.TABLE_VELKOMPC);
    }
    
    private void setPassPref() {
        Preferences pref = AppComponents.getUserPref();
        pref.put(PropertiesNames.DBUSER, DataConnectTo.DBUSER_KUDR);
        pref.put(PropertiesNames.DBPASS, "36e42yoak8");
        try {
            pref.sync();
        }
        catch (BackingStoreException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    @Ignore
    public void writeLocalDBFromFile() {
        int lastId = getLastID("velkompc", "idvelkompc");
        System.out.println("lastId = " + lastId);
        try (InputStream inputStream = new FileInputStream("velkompc.table");
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            bufferedReader.lines().forEach(x->{
                String[] arrOfLine = x.split(",");
                int id = 0;
                try {
                    
                    id = Integer.parseInt(arrOfLine[0]);
                }
                catch (NumberFormatException e) {
                    id = 0;
                }
                if (id > lastId) {
                    writeToLocalDB(arrOfLine);
                }
            });
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    private int getLastID(String dbToSync, String idColName) {
        int retInt = 20000000;
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
        MysqlDataSource source = dataConnectTo.getDataSource();
        source.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        try (Connection connection = source.getConnection()) {
            String sql = "select " + idColName + " from " + dbToSync + " ORDER BY " + idColName + " DESC LIMIT 1";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        if (resultSet.last()) {
                            retInt = resultSet.getInt(idColName);
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        return retInt;
    }
    
    private static void writeToLocalDB(@NotNull String[] arrFromStringInFileDumpedFromRemote) {
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
    
    private static void writeLocalOverRemote(int idvelkompc, @NotNull ResultSet resultSet) {
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
    
    private static void writeToLocalFile(int idvelkompc, @NotNull ResultSet resultSet) {
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