package ru.vachok.networker.data.synchronizer;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.TForms;
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
        System.out.println("syncResult = " + syncResult);
    }
    
    @Test
    @Ignore
    public void testSuperRun() {
        syncData.superRun();
    }
    
    @Test
    public void testUploadCollection() {
        try {
            int rowsUp = syncData.uploadCollection(Collections.singleton("test"), "test");
            System.out.println("rowsUp = " + rowsUp);
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
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
        Assert.assertEquals(toStr, "InternetSync[\n" +
            "ipAddr = '10.10.35.30'\n" +
            "]");
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
    
    @Test
    public void testCreateTable() {
        int syncDataTable = new InternetSync("10.10.10.30").createTable("inetstats.test", Collections.emptyList());
        Assert.assertEquals(syncDataTable, 0);
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