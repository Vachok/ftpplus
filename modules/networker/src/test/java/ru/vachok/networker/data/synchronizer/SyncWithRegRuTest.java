package ru.vachok.networker.data.synchronizer;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Queue;


/**
 @see SyncWithRegRu
 @since 08.09.2019 (14:56) */
public class SyncWithRegRuTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(SyncWithRegRu.class.getSimpleName(), System
        .nanoTime());
    
    private String aboutWhat = "192.168.13.220";
    
    private DBStatsUploader dbStatsUploader;
    
    private SyncWithRegRu syncWithRegRu = new SyncWithRegRu();
    
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
    public void modelingTest() {
        Assert.assertTrue(aboutWhat.matches(String.valueOf(ConstantsFor.PATTERN_IP)), aboutWhat + " is not IP!");
        
        Path rootPath = Paths.get(".");
        String statFile = ConstantsFor.FILESYSTEM_SEPARATOR + "inetstats" + ConstantsFor.FILESYSTEM_SEPARATOR + aboutWhat.replaceAll("_", ".") + ".csv";
        String inetStatsPath = rootPath.toAbsolutePath().normalize().toString() + statFile;
        Assert.assertTrue(new File(inetStatsPath).exists(), inetStatsPath + " does not exists");
    
        Queue<String> queueFromFile = getLimitQueueFromFile(Paths.get(inetStatsPath));
        Assert.assertTrue(queueFromFile.size() > 0, MessageFormat.format("{0} queueFromFile: {1}", queueFromFile.size(), inetStatsPath));
    }
    
    @Test
    public void testSyncData() {
        SyncData syncData = SyncData.getInstance();
        try {
            String data = syncData.syncData();
        }
        catch (IllegalArgumentException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testToString() {
        String toString = syncWithRegRu.toString();
        Assert.assertTrue(toString.contains("SyncWithRegRu["), toString);
    }
    
    private @NotNull Queue<String> getLimitQueueFromFile(Path filePath) {
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
        Assert.assertTrue(dataConnectTo.toString().contains("MySqlLocalSRVInetStat"), dataConnectTo.toString());
        
        Queue<String> statAbout = FileSystemWorker.readFileToQueue(filePath);
        int fileSize = statAbout.size();
        try (Connection connection = dataConnectTo.getDefaultConnection("inetstats");
             PreparedStatement preparedStatement = connection.prepareStatement("select idrec from " + aboutWhat.replaceAll("\\Q.\\E", "_") + " ORDER BY idrec DESC LIMIT 1");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                int idrec = resultSet.getInt("idrec");
                for (int i = 0; i < idrec; i++) {
                    statAbout.poll();
                }
            }
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Assert.assertTrue(fileSize > statAbout.size(), MessageFormat.format("File size: {0} strings. Polled queue size: {1}.", fileSize, statAbout.size()));
        return statAbout;
    }
    
    private void parseQueue(@NotNull String[] valuesArr) {
        Assert.assertTrue(valuesArr.length == 5, new TForms().fromArray(valuesArr));
        dbStatsUploader.setOption(aboutWhat);
        dbStatsUploader.setOption(Arrays.asList(valuesArr));
    }
}