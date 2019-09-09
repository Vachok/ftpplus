package ru.vachok.networker.data.synchronizer;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.io.*;
import java.sql.*;
import java.util.concurrent.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @see DBSyncronizer
 @since 08.09.2019 (17:46) */
public class DBSyncronizerTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(DBSyncronizer.class.getSimpleName(), System.nanoTime());
    
    private DBSyncronizer dbSyncronizer = new DBSyncronizer(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    
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
    public void testTestToString() {
        String s = dbSyncronizer.toString();
        Assert.assertTrue(s.contains("DBSyncronizer["), s);
    }
    
    @Test
    public void testSyncDataOverDB() {
        String overDB = dbSyncronizer.syncDataOverDB("inetstats");
        Assert.assertTrue(overDB.contains("Column 'idvelkompc' not found"), overDB);
    }
    
    @Test
    public void testWriteLocalDBFromFile() {
        throw new InvokeEmptyMethodException("WriteLocalDBFromFile created 08.09.2019 at 17:46");
    }
    
    @Test
    @Ignore
    public void testTestSyncDB() {
        Runnable runnable = ()->SyncData.syncDB(ConstantsFor.TABLE_VELKOMPC);
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
        SyncData.syncDB(ConstantsFor.TABLE_VELKOMPC);
    }
    
    @Test
    @Ignore
    public void writeLocalDBFromFile() {
        int lastId = getLastID("velkompc", "IDVELKOMPC".toLowerCase());
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
    
    private static int getLastID(String dbToSync, String idColName) {
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