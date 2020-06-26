package ru.vachok.networker.info.stats;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.*;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.synchronizer.SyncData;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.io.File;
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

    private InternetSync syncData;

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
        syncData = (InternetSync) SyncData.getInstance("192.168.13.220");
        this.connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection("inetstats." + syncData.getDbToSync().replaceAll("\\Q.\\E", "_"));
    }

    @Test
    public void testSyncData() {
        Path rootPath = Paths.get(".").toAbsolutePath().normalize();
        File testFile = new File(rootPath
            .toString() + ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.DIR_INETSTATS + ConstantsFor.FILESYSTEM_SEPARATOR + "10.200.213.98.csv");
        File okDir = new File(testFile.toPath().toAbsolutePath().getParent().toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + "ok");
        Assert.assertTrue(okDir.exists());
        Assert.assertTrue(okDir.isDirectory());
        if (!testFile.exists()) {
            try {
                FileSystemWorker
                    .copyOrDelFile(new File(okDir.getAbsolutePath() + ConstantsFor.FILESYSTEM_SEPARATOR + "10.200.213.98-11.txt"), testFile.toPath().toAbsolutePath()
                        .normalize(), false);
            }
            catch (InvokeIllegalException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
            }
        }
    
        Object o = AppConfigurationLocal.executeInWorkStealingPool(new InternetSync("192.168.13.220"), 75);
        Assert.assertTrue(o.toString().contains("No original FILE! 192.168.13.220.csv"), o.toString());
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
            if (UsefulUtilities.thisPC().toLowerCase().contains("rups")) {
                Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
            }
            else {
                Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
            }
        }
        catch (TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }

    @Test
    @Ignore
    public void testCreateTable$$COPY() {
        String tableCreate = syncData.createTable("10.200.213.200");
        Assert.assertEquals(tableCreate, "Updated: 0. Query: \n" +
                "CREATE TABLE if not exists `10_200_213_200` (\n" +
                "\t`idrec` MEDIUMINT(11) UNSIGNED NOT NULL AUTO_INCREMENT,\n" +
                "\t`stamp` BIGINT(13) UNSIGNED NOT NULL DEFAULT '442278000000',\n" +
                "\t`squidans` VARCHAR(20) NOT NULL DEFAULT 'unknown',\n" +
                "\t`bytes` INT(11) NOT NULL DEFAULT '42',\n" +
                "\t`timespend` INT(11) NOT NULL DEFAULT '42',\n" +
                "\t`site` VARCHAR(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',\n" +
                "\tPRIMARY KEY (`idrec`),\n" +
                "\tUNIQUE INDEX `stamp` (`stamp`, `site`, `bytes`) USING HASH\n" +
                ")\n" +
                "COMMENT='do0045 : kpivovarov'\n" +
                "COLLATE='utf8_general_ci'\n" +
                "ENGINE=MyISAM\n" +
                "ROW_FORMAT=COMPRESSED\n" +
                ";");
    }

    @Test
    @Ignore
    public void testComments() {
        InternetSync internetSync = new InternetSync("10_200_213_85");
        String checkComment = internetSync.checkComment();
        Assert.assertEquals(checkComment, "do0213 : ikudryashov");
    }

    @Test
    public void testCreateTable() {
        Path rootP = Paths.get(".");
        File rootF = new File(rootP.toAbsolutePath().normalize()
            .toString() + ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.DIR_INETSTATS + ConstantsFor.FILESYSTEM_SEPARATOR + "10.200.213.98.csv");
        if (!rootF.exists()) {
            FileSystemWorker.writeFile(rootF.getAbsolutePath(), getSample());
        }
        Assert.assertTrue(rootF.exists());
        String tableCreate = new InternetSync("10.200.213.98").createTable("10.200.213.98");
        if (UsefulUtilities.thisPC().toLowerCase().contains("do")) {
            Assert.assertTrue(tableCreate.contains("Updated: 0. Query: \n" + "CREATE TABLE if not exists `10_200_213_98` (\n"));
        }
        else {
            Assert.assertTrue(tableCreate.contains("Updated: 0. Query: \n" +
                "CREATE TABLE if not exists `10_200_213_98` (\n" +
                "\t`idrec` MEDIUMINT(11) UNSIGNED NOT NULL AUTO_INCREMENT,\n" +
                "\t`stamp` BIGINT(13) UNSIGNED NOT NULL DEFAULT '442278000000',\n" +
                "\t`squidans` VARCHAR(20) NOT NULL DEFAULT 'unknown',\n" +
                "\t`bytes` INT(11) NOT NULL DEFAULT '42',\n" +
                "\t`timespend` INT(11) NOT NULL DEFAULT '42',\n" +
                "\t`site` VARCHAR(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',\n" +
                "\tPRIMARY KEY (`idrec`),\n" +
                "\tUNIQUE INDEX `stampkey` (`stamp`, `site`, `bytes`) USING BTREE\n" +
                ")"));

        }
    }

    @Contract(pure = true)
    private static @NotNull String getSample() {
        return "\n" +
                "\n" +
                "Fri Jun 07 12:55:22 MSK 2019,TCP_TUNNEL/200,540,CONNECT,meyou.ru:443<br><br>\n" +
                "Sat Jun 08 17:21:08 MSK 2019,TCP_DENIED/403,3933,CONNECT,ping3.teamviewer.com:443<br><br>\n" +
                "Mon Jun 03 18:41:23 MSK 2019,TCP_DENIED/403,3933,CONNECT,ping3.teamviewer.com:443<br><br>\n" +
                "Fri Jun 07 20:31:44 MSK 2019,TCP_MISS/200,245,GET,http://www.msftncsi.com/ncsi.txt<br><br>\n" +
                "Fri Jun 07 21:43:19 MSK 2019,TCP_DENIED/403,4135,GET,http://ctldl.windowsupdate.com/msdownload/update/v3/static/trustedr/en/disallowedcertstl.cab?<br><br>\n" +
                "Fri Jun 28 09:29:59 MSK 2019,TCP_MISS/503,4031,GET,http://config.messenger.msn.com/config/msgrconfig.asmx?\n" +
                "Sat Jun 08 17:21:08 MSK 2019,TCP_DENIED/403,4002,GET,http://master3.teamviewer.com/din.aspx?<br><br>\n" +
                "Fri Jun 07 12:55:19 MSK 2019,TCP_TUNNEL/200,3944,CONNECT,connect.mail.ru:443<br><br>\n" +
                "Thu Jul 18 09:50:10 MSK 2019,TCP_TUNNEL/200,4928,CONNECT,www.google-analytics.com:443\n";
    }

    @Test
    public void testUploadCollection() {
        int rowsUp = syncData.uploadCollection(Collections.singleton("test"), "1.1.1.1");
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
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }

    @Test
    public void testToString() {
        String toStr = syncData.toString();
        Assert.assertEquals(toStr, "InternetSync{ipAddr='192.168.13.220', dbFullName='inetstats.192_168_13_220', connection=\n" +
            "}");
    }

    @Test
    @Ignore
    public void logicTest() {
        Path filePath = Paths.get(".");
        filePath = Paths
            .get(filePath.toAbsolutePath().normalize()
                .toString() + ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.DIR_INETSTATS + ConstantsFor.FILESYSTEM_SEPARATOR + syncData
                .getDbToSync() + ".csv");
        Queue<String> fileQueue = FileSystemWorker.readFileToQueue(filePath);
        Assert.assertTrue(fileQueue.size() > 0);
        if (createJSON(fileQueue) > 0) {
            fileWork(filePath);
        }
    }

    @Test
    public void testGetRawResult() {
        Object result = syncData.getRawResult();
        Assert.assertTrue(result.toString().contains("192.168.13.220"), result.toString());
    }

    @Test
    public void testRun() {
        AppConfigurationLocal.getInstance().execute(syncData, 20);
    }

    @Test
    public void testExec() {
        boolean isExec = (boolean) AppConfigurationLocal.getInstance().executeGet(()->syncData.exec(), 50);
        Assert.assertTrue(isExec);
    }

    @Test
    public void testCheckComment() {
        String s = syncData.checkComment();
        Assert.assertTrue(s.contains("Last online"), s);
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
                jsonObject.add(ConstantsFor.DBCOL_BYTES, toJSON[2]);
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
            jsonObject.add(ConstantsFor.DBCOL_BYTES, "1");
            jsonObject.add("site", ConstantsFor.SITE_VELKOMFOOD);
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
            preparedStatement.setInt(3, Integer.parseInt(object.get(ConstantsFor.DBCOL_BYTES).asString()));
            preparedStatement.setString(4, object.get("site").toString().replaceAll("\\Q\"\\E", ""));
            result = preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            if (e.getMessage().contains(ConstantsFor.ERROR_DUPLICATEENTRY)) {
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