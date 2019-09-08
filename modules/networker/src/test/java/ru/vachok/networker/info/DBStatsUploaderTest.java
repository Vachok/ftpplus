package ru.vachok.networker.info;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Queue;


/**
 @see DBStatsUploader
 @since 08.09.2019 (10:16) */
public class DBStatsUploaderTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(DBStatsUploader.class.getSimpleName(), System.nanoTime());
    
    private DataConnectTo dataConnectTo;
    
    private DBStatsUploader dbStatsUploader = new DBStatsUploader();
    
    private String aboutWhat;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        this.dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testGetInfoAbout() {
        throw new InvokeEmptyMethodException("GetInfoAbout created 08.09.2019 at 10:12");
    }
    
    @Test
    public void testGetInfo() {
        String info = dbStatsUploader.getInfo();
        System.out.println("info = " + info);
    }
    
    @Test
    public void testTestToString() {
        String toStr = dbStatsUploader.toString();
        Assert.assertTrue(toStr.contains("datasource=jdbc:mysql://srv-inetstat.eatmeat.ru:3306/"), toStr);
    }
    
    @Test
    public void testCreateUploadStatTable() {
        this.aboutWhat = "10.200.210.64";
        dbStatsUploader.setClassOption(aboutWhat);
        int i = dbStatsUploader.createUploadStatTable(new String[0]);
        Assert.assertTrue(i == 0);
    }
    
    @Test
    public void testUploadToTable() {
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
        dbStatsUploader.uploadToTable(valuesArr);
    }
    
    @Test
    public void parseStamp() {
        String stringWithDate = "Thu Aug 01 05:38:48 MSK 2019";
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);
        String formatExpect = format.format(new Date());
        System.out.println("formatExpect = " + formatExpect);
        Date parsedDate;
        try {
            
            parsedDate = format.parse(stringWithDate);
            System.out.println("parsedDate = " + parsedDate);
        }
        catch (ParseException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    private @NotNull String makePathStr() {
        this.aboutWhat = "10.200.210.64".replaceAll("\\Q.\\E", "_");
        Path rootPath = Paths.get(".");
        rootPath = Paths.get(rootPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + "inetstats");
        Assert.assertTrue(rootPath.toFile().getName().equalsIgnoreCase("inetstats"));
        makeTable(rootPath.toAbsolutePath().normalize().toString());
        return rootPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + aboutWhat.replaceAll("_", ".") + ".csv";
    }
    
    private void makeTable(String name) {
        String[] sqlS = {
            "ALTER TABLE " + aboutWhat + "\n" +
                "  ADD PRIMARY KEY (`idrec`),\n" +
                "  ADD UNIQUE KEY `stamp` (`stamp`,`ip`,`bytes`) USING BTREE,\n" +
                "  ADD KEY `ip` (`ip`);",
            
            "ALTER TABLE " + aboutWhat + "\n" +
                "  MODIFY `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '';"};
        dbStatsUploader.createUploadStatTable(sqlS);
    }
    
    private void getTables(@NotNull Path rootPath) {
        File[] inetFiles = rootPath.toFile().listFiles();
        for (File statCsv : inetFiles) {
            String tableName = statCsv.getName().replaceAll("\\Q.\\E", "_").replace("_csv", "");
            System.out.println("tableName = " + tableName);
            dbStatsUploader.setClassOption(tableName);
            this.aboutWhat = tableName;
        }
    }
}