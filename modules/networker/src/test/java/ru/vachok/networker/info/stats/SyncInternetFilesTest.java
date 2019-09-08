package ru.vachok.networker.info.stats;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 @see SyncInternetFiles
 @since 08.09.2019 (14:56) */
public class SyncInternetFilesTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(SyncInternetFiles.class.getSimpleName(), System
        .nanoTime());
    
    private String aboutWhat;
    
    private DataConnectTo dataConnectTo;
    
    private DBStatsUploader dbStatsUploader;
    
    private SyncInternetFiles syncInternetFiles = new SyncInternetFiles();
    
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
    public void testGetInfo() {
        Future<String> info = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(()->syncInternetFiles.getInfo());
        try {
            info.get(10, TimeUnit.SECONDS);
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
    public void testGetInfoAbout() {
        try {
            String infoAbout = syncInternetFiles.getInfoAbout(aboutWhat);
        }
        catch (TODOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    private void testUploadToTable() {
        this.aboutWhat = "10.200.210.64";
        Path rootPath = Paths.get(".");
        String inetStatsPath = rootPath.toAbsolutePath().normalize()
            .toString() + ConstantsFor.FILESYSTEM_SEPARATOR + "inetstats" + ConstantsFor.FILESYSTEM_SEPARATOR + aboutWhat.replaceAll("_", ".") + ".csv";
        Queue<String> statAbout = limitedQueue(inetStatsPath);
        Assert.assertTrue(statAbout.size() > 0);
        
        while (!statAbout.isEmpty()) {
            String entryStat = statAbout.poll();
            if (entryStat.isEmpty() || !entryStat.contains(",")) {
                continue;
            }
            String[] valuesArr = entryStat.split(",");
            parseQueue(valuesArr);
        }
    }
    
    private @NotNull Queue<String> limitedQueue(String inetStatsPath) {
        Paths.get(inetStatsPath);
        Queue<String> statAbout = FileSystemWorker.readFileToQueue(Paths.get(inetStatsPath));
        try (Connection connection = dataConnectTo.getDefaultConnection("inetstats");
             PreparedStatement preparedStatement = connection
                 .prepareStatement("select idrec from " + aboutWhat.replaceAll("\\Q.\\E", "_") + " ORDER BY idrec DESC LIMIT 1");
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
        return statAbout;
    }
    
    private void parseQueue(@NotNull String[] valuesArr) {
        Assert.assertTrue(valuesArr.length == 5, new TForms().fromArray(valuesArr));
        dbStatsUploader.setClassOption(aboutWhat);
        dbStatsUploader.setClassOption(Arrays.asList(valuesArr));
        dbStatsUploader.getInfo();
    }
    
    
}