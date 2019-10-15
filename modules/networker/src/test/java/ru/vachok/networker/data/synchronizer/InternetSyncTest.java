package ru.vachok.networker.data.synchronizer;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.*;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.concurrent.*;


/**
 @see InternetSync
 @since 13.10.2019 (13:24) */
public class InternetSyncTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(InternetSync.class.getSimpleName(), System.nanoTime());
    
    private SyncData syncData;
    
    private Connection connection;
    
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
    public void initSync() {
        syncData = SyncData.getInstance("10.200.208.65");
        this.connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection("inetstats." + syncData.getDbToSync().replaceAll("\\Q.\\E", "_"));
    }
    
    @Test
    public void testSyncData() {
        String syncResult = syncData.syncData();
        Assert.assertTrue(syncResult.contains("0.200.208.65.csv created 0 rows"));
    }
    
    @Test
    public void testSuperRun() {
        Future<?> superRunFuture = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(()->syncData.superRun());
        try {
            superRunFuture.get(7, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        catch (TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void testCreateTable() {
        String tableCreate = ((InternetSync) syncData).createTable("10.200.213.200");
        Assert.assertEquals(tableCreate, "Updated: 0. Query: \n" +
                "CREATE TABLE if not exists `10_200_213_200` (\n" +
                "\t`idrec` MEDIUMINT(11) UNSIGNED NOT NULL AUTO_INCREMENT,\n" +
                "\t`stamp` BIGINT(13) UNSIGNED NOT NULL DEFAULT '442278000000',\n" +
                "\t`squidans` VARCHAR(20) NOT NULL DEFAULT 'unknown',\n" +
                "\t`bytes` INT(11) NOT NULL DEFAULT '42',\n" +
                "\t`timespend` INT(11) NOT NULL DEFAULT '42',\n" +
                "\t`site` VARCHAR(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',\n" +
                "\tPRIMARY KEY (`idrec`),\n" +
                "\tUNIQUE INDEX `stamp` (`stamp`, `site`, `bytes`) USING BTREE,\n" +
                "\tINDEX `site` (`site`)\n" +
                ")\n" +
                "COMMENT='do0045 : kpivovarov'\n" +
                "COLLATE='utf8_general_ci'\n" +
                "ENGINE=MyISAM\n" +
                "CHECKSUM=1\n" +
                ";");
    }
    
    @Test
    public void testUploadCollection() {
        try {
            int rowsUp = syncData.uploadCollection(Collections.singleton("test"), "test");
        }
        catch (InvokeIllegalException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        int upInt = syncData.uploadCollection(Collections
                .singletonList("Fri Jun 07 17:48:33 MSK 2019,TCP_MISS/200,4794,GET,http://tile-service.weather.microsoft.com/ru-RU/livetile/preinstall?<br<br\n"), "10.10.30.30");
        Assert.assertTrue(upInt == 0);
    }
    
    @Test
    public void testMakeColumns() {
        try {
            Map<String, String> map = syncData.makeColumns();
            System.out.println("map = " + AbstractForms.fromArray(map));
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testToString() {
        String toStr = syncData.toString();
        Assert.assertEquals(toStr, "InternetSync{ipAddr='10.200.208.65', dbFullName='inetstats.10_200_208_65', connection=}");
    }
    
    @Test
    @Ignore
    public void logicTest() {
        Path filePath = Paths.get(".");
        filePath = Paths
                .get(filePath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + "inetstats" + ConstantsFor.FILESYSTEM_SEPARATOR + syncData
                        .getDbToSync() + ".csv");
        Queue<String> fileQueue = FileSystemWorker.readFileToQueue(filePath);
        Assert.assertTrue(fileQueue.size() > 0);
        if (createJSON(fileQueue) > 0) {
            fileWork(filePath);
        }
    }
    
    private int createJSON(@NotNull Queue<String> fileQueue) {
        int updatedRows = 0;
        while (!fileQueue.isEmpty()) {
            String removedStr = fileQueue.remove();
            String[] toJSON = new String[5];
            try {
                toJSON = removedStr.split(",");
            }
            catch (IndexOutOfBoundsException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
            JsonObject jsonObject = new JsonObject();
            long valueParsed = parseDate(toJSON[0]);
            if (valueParsed < 0) {
                jsonObject = parseAsObject(removedStr);
            }
            else {
                jsonObject.add("stamp", String.valueOf(valueParsed));
                jsonObject.add("squidans", toJSON[1]);
                jsonObject.add("bytes", toJSON[2]);
                jsonObject.set("site", toJSON[4]);
            }
            JsonObject finalJsonObject = jsonObject;
            updatedRows += sendToDatabase(finalJsonObject);
            System.out.println("fileQueue = " + fileQueue.size());
        }
        return updatedRows;
    }
    
    private void fileWork(Path filePath) {
        try {
            Path movedFilePath = Files.move(filePath, Paths.get(filePath.toAbsolutePath().normalize().toString().replace(".csv", ".txt")));
            System.out.println("movedFilePath = " + movedFilePath);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Contract(pure = true)
    private static long parseDate(String dateAsString) {
        long result;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy", Locale.ENGLISH);
        try {
            Date parsedDate = simpleDateFormat.parse(dateAsString);
            System.out.println("parsedDate = " + parsedDate);
            result = parsedDate.getTime();
        }
        catch (ParseException e) {
            result = -1;
        }
        return result;
    }
    
    private JsonObject parseAsObject(String str) {
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject = (JsonObject) Json.parse(str);
            return jsonObject;
        }
        catch (com.eclipsesource.json.ParseException e) {
            jsonObject.add("stamp", "1");
            jsonObject.add("squidans", "");
            jsonObject.add("bytes", "1");
            jsonObject.add("site", "velkomfood.ru");
            return jsonObject;
        }
    }
    
    private int sendToDatabase(@NotNull JsonObject object) {
        int result;
        DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
        String dbName = syncData.getDbToSync().replaceAll("\\Q.\\E", "_");
        final String sql = String.format("insert into %s (stamp, squidans, bytes, site) values (?, ?, ?, ?)", dbName);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            long timestampLong = Long.parseLong(object.get("stamp").asString());
            preparedStatement.setLong(1, timestampLong);
            preparedStatement.setString(2, object.get("squidans").toString().replaceAll("\\Q\"\\E", ""));
            preparedStatement.setInt(3, Integer.parseInt(object.get("bytes").asString()));
            preparedStatement.setString(4, object.get("site").toString().replaceAll("\\Q\"\\E", ""));
            result = preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                result = 0;
            }
            else {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
                result = -2;
            }
        }
        return result;
    }
}