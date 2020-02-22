package ru.vachok.networker.exe.runnabletasks;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.ParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.AppInfoOnLoad;
import ru.vachok.networker.ad.common.RightsChecker;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.DeleterTemp;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.services.LocalDBLibsUploader;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.componentsrepo.services.RegRuFTPLibsUploader;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.stats.Stats;
import ru.vachok.networker.net.scanner.PcNamesScanner;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static java.time.DayOfWeek.SUNDAY;


/**
 @see OnStartTasksLoaderTest
 @since 14.11.2019 (10:58) */
public class OnStartTasksLoader implements AppConfigurationLocal {


    private static final String DBNAME_LOG_DBMESSENGER = "log.dbmessenger";

    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, OnStartTasksLoader.class.getSimpleName());

    @Override
    public void run() {
        delFilePatterns();
        if (UsefulUtilities.thisPC().toLowerCase().contains("home") && UsefulUtilities.thisPC().toLowerCase()
            .contains(OtherKnownDevices.DO0213_KUDR.replace(ConstantsFor.DOMAIN_EATMEATRU, ""))) {
            uploadLibs();
        }
        execute(new PcNamesScanner());
        schedule(this::dbSendAppJson, 30);
        execute(this::getWeekPCStats);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OnStartTasksLoader{");
        sb.append("messageToUser=").append(messageToUser);
        sb.append('}');
        return sb.toString();
    }

    private void uploadLibs() {
        File[] libFiles = new File(Paths.get(".").normalize().toAbsolutePath().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + "lib").listFiles();
        for (File libFile : Objects.requireNonNull(libFiles, "NO LIBS")) {
            String libName = libFile.getName().split("-")[0];
            String libVersion = libFile.getName().split("-")[1].split("\\Q.\\E")[0];
            Runnable jarUp = new LocalDBLibsUploader(libName, libVersion, "jar", libFile.toPath());
            execute(jarUp);
        }
    }

    private void delFilePatterns() {
        DeleterTemp deleterTemp = new DeleterTemp(UsefulUtilities.getPatternsToDeleteFilesOnStart());
        execute(deleterTemp);
        ftpUploadTask();
    }

    private void ftpUploadTask() {
        messageToUser.warn(PropertiesNames.SYS_OSNAME_LOWERCASE);
        AppInfoOnLoad.getMiniLogger().add(UsefulUtilities.thisPC());
        try {
            String ftpUpload = "new AppComponents().launchRegRuFTPLibsUploader() = " + launchRegRuFTPLibsUploader();
            AppInfoOnLoad.getMiniLogger().add(ftpUpload);
        }
        catch (RuntimeException e) {
            messageToUser.error("OnStartTasksLoader.ftpUploadTask", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
        finally {
            if (System.getProperty("os.name").toLowerCase().contains(PropertiesNames.WINDOWSOS)) {
                runCommonScan();
            }
        }
    }

    @NotNull
    private String launchRegRuFTPLibsUploader() {
        Runnable regRuFTPLibsUploader = new RegRuFTPLibsUploader();
        try {
            execute(regRuFTPLibsUploader);
            return this.getClass().getSimpleName() + ".launchRegRuFTPLibsUploader: TRUE";
        }
        catch (RuntimeException e) {
            return MessageFormat.format("{0}.launchRegRuFTPLibsUploader: FALSE {1} {2}",
                AppComponents.class.getSimpleName(), e.getMessage(), Thread.currentThread().getState().name());
        }
        finally {
            dbSendAppJson();
        }
    }

    private void getWeekPCStats() {
        if (LocalDate.now().getDayOfWeek().equals(SUNDAY)) {
            Stats stats = Stats.getInstance(InformationFactory.STATS_WEEKLY_INTERNET);
            AppConfigurationLocal.getInstance().execute((Runnable) stats);
            stats = Stats.getInstance(InformationFactory.STATS_SUDNAY_PC_SORT);
            try {
                String pcStats = stats.call();
                System.out.println("pcStats = " + pcStats);
            }
            catch (RuntimeException e) {
                messageToUser.error(MessageFormat.format("AppInfoOnLoad.getWeekPCStats {0} - {1}", e.getClass().getTypeName(), e.getMessage()));
            }
            finally {
                messageToUser.warn(this.getClass().getSimpleName(), "getWeekPCStats", toString());
            }
        }
    }

    private void runCommonScan() {
        Runnable checker = buildChecker();
        try {
            Files.deleteIfExists(Paths.get(FileNames.COMMON_RGH));
            Files.deleteIfExists(Paths.get(FileNames.COMMON_OWN));
        }
        catch (IOException e) {
            messageToUser.warn(OnStartTasksLoader.class.getSimpleName(), "runCommonScan", e.getMessage() + Thread.currentThread().getState().name());
        }
        finally {
            Date day2030 = MyCalen.getThisDay(20, 30);
            long delayOneDay = TimeUnit.DAYS.toMillis(1);
            AppComponents.threadConfig().getTaskScheduler().scheduleAtFixedRate(checker, day2030, delayOneDay);
        }

    }

    private void dbSendAppJson() {
        DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
        dataConnectTo.createTable(DBNAME_LOG_DBMESSENGER, Collections.emptyList());
        Path path = Paths.get(FileNames.APP_JSON);
        final String sql = "INSERT INTO log.dbmessenger (`tstamp`, `upstring`, `json`) VALUES (?, ?, ?)";
        try (Connection connection = dataConnectTo.getDefaultConnection(DBNAME_LOG_DBMESSENGER)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                Queue<String> logJson;
                if (path.toFile().exists()) {
                    logJson = FileSystemWorker.readFileToQueue(path);
                }
                else {
                    throw new InvokeIllegalException(this.getClass().getSimpleName() + "dbSendAppJson");
                }
                while (!logJson.isEmpty()) {
                    executeStatement(preparedStatement, logJson);
                }
                Files.deleteIfExists(path);
            }
        }
        catch (SQLException | IOException e) {
            if (!e.getMessage().contains(ConstantsFor.STR_DUPLICATE)) {
                messageToUser.error("OnStartTasksLoader.dbSendAppJson", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            }
        }
    }

    private void executeStatement(@NotNull PreparedStatement preparedStatement, @NotNull Queue<String> logJson) throws SQLException {
        String jsonStr = logJson.remove();
        try {
            JsonObject jsonObject = Json.parse(jsonStr).asObject();
            long jsonTimeStamp = jsonObject.getLong(PropertiesNames.TIMESTAMP, 0);
            jsonObject.remove(PropertiesNames.TIMESTAMP);
            Timestamp sqlTimestamp = Timestamp.valueOf(LocalDateTime.ofEpochSecond((jsonTimeStamp / 1000), 0, ZoneOffset.ofHours(3)));
            preparedStatement.setTimestamp(1, sqlTimestamp);
            preparedStatement.setString(2, UsefulUtilities.thisPC());
            preparedStatement.setString(3, jsonObject.toString());
            preparedStatement.executeUpdate();
        }
        catch (ParseException ignore) {
            //08.01.2020 (5:43)
        }
    }

    @Contract(" -> new")
    @NotNull
    private Runnable buildChecker() {
        Path pathStart = Paths.get("\\\\srv-fs.eatmeat.ru\\it$$\\Хлам\\");
        Path pathToSaveLogs = Paths.get(".");
        if (UsefulUtilities.thisPC().toLowerCase().contains("rups")) {
            pathStart = Paths.get("\\\\srv-fs.eatmeat.ru\\common_new");
            pathToSaveLogs = Paths.get("\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Внутренняя");
        }
        return new RightsChecker(pathStart, pathToSaveLogs);
    }
}