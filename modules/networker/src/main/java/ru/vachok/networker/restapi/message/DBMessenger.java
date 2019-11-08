// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.message;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.restapi.message.DBMessengerTest
 @since 26.08.2018 (12:29) */
public class DBMessenger implements MessageToUser {
    
    
    private final Runnable dbSendRun = this::dbSend;
    
    private String headerMsg;
    
    private String titleMsg;
    
    private String bodyMsg;
    
    private boolean isInfo = true;
    
    public void setHeaderMsg(String headerMsg) {
        this.headerMsg = headerMsg;
    }
    
    @Contract(pure = true)
    DBMessenger(String headerMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = DataConnectTo.getInstance(DataConnectTo.TESTING).toString();
        AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor()
            .scheduleWithFixedDelay(this::dbSendFile, 0, ConstantsFor.DELAY, TimeUnit.MINUTES);
    }
    
    private void dbSendFile() {
        DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
        dataConnectTo.createTable(ConstantsFor.DBNAME_LOG_DBMESSENGER, Collections.emptyList());
        final String sql = "INSERT INTO log.dbmessenger (`tstamp`, `json`) VALUES (?, ?)";
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DBNAME_LOG_DBMESSENGER);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            Path path = Paths.get(FileNames.APP_JSON);
            Queue<String> logJson = FileSystemWorker.readFileToQueue(path);
            while (!logJson.isEmpty()) {
                String jsonStr = logJson.remove();
                if (!jsonStr.isEmpty()) {
                    JsonObject jsonObject = Json.parse(jsonStr).asObject();
                    preparedStatement.setTimestamp(1, Timestamp
                        .valueOf(LocalDateTime.ofEpochSecond((jsonObject.getLong(PropertiesNames.TIMESTAMP, 0) / 1000), 0, ZoneOffset.ofHours(3))));
                    preparedStatement.setString(2, jsonObject.toString());
                    preparedStatement.executeUpdate();
                }
                
            }
            Files.deleteIfExists(path);
        }
        catch (SQLException | IOException e) {
            System.err.println(MessageFormat.format("{0}, message: {1}. See line: 176 ***", DBMessenger.class.getSimpleName(), e.getMessage()));
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DBMessenger{");
        sb.append("titleMsg='").append(titleMsg).append('\'');
        String sendResult = "No sends ";
        sb.append(", sendResult='").append(sendResult).append('\'');
        sb.append(", isInfo=").append(isInfo);
        sb.append(", headerMsg='").append(headerMsg).append('\'');
        sb.append(", bodyMsg='").append(bodyMsg).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    private void dbSend() {
        String sql = "insert into log.networker (classname, msgtype, msgvalue, pc, stack, upstring) values (?,?,?,?,?,?)";
        String pc = UsefulUtilities.thisPC() + " : " + UsefulUtilities.getUpTime();
        if (!isInfo) {
            sql = sql.replace(ConstantsFor.PREF_NODE_NAME, "errors");
        }
        dbConnect(sql, pc, getStack());
    }
    
    @Override
    public void errorAlert(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        this.isInfo = false;
        AppComponents.threadConfig().getTaskExecutor().execute(dbSendRun, 1);
    }
    
    private void dbConnect(String sql, String pc, String stack) {
        try (Connection con = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection("log")) {
            try (PreparedStatement p = con.prepareStatement(sql)) {
                p.setString(1, this.headerMsg);
                p.setString(2, this.titleMsg);
                p.setString(3, this.bodyMsg);
                p.setString(4, pc);
                p.setString(5, stack);
                p.setString(6, String.valueOf(LocalTime.now()));
                p.executeUpdate();
            }
        }
        catch (SQLException | RuntimeException e) {
            if (!e.getMessage().contains(ConstantsFor.ERROR_DUPLICATEENTRY)) {
                notDuplicate();
            }
        }
    }
    
    private @NotNull String getStack() {
        StringBuilder stringBuilder = new StringBuilder();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        stringBuilder.append(AbstractForms.fromArray(threadMXBean.dumpAllThreads(true, true)));
        return stringBuilder.toString();
    }
    
    @Override
    public void info(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        this.isInfo = true;
        AppComponents.threadConfig().getTaskExecutor().execute(dbSendRun, 1);
    }
    
    private void notDuplicate() {
        MessageToUser msgToUsr = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
        String titleBody = MessageFormat.format("Title: {0}\nBody: {1}", this.titleMsg, this.bodyMsg);
        msgToUsr.warn(this.getClass().getSimpleName() + "->" + this.headerMsg, "send log error!", titleBody);
        this.headerMsg = "";
        this.bodyMsg = "";
        this.titleMsg = "";
    }
    
    @Override
    public void infoNoTitles(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        info(this.headerMsg, this.titleMsg, this.bodyMsg);
    }
    
    @Override
    public void info(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        info(this.headerMsg, this.titleMsg, bodyMsg);
    }
    
    @Override
    public void error(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        this.headerMsg = PropertiesNames.ERROR;
        this.titleMsg = this.getClass().getSimpleName();
        errorAlert(this.headerMsg, this.titleMsg, bodyMsg);
    }
    
    @Override
    public void error(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        errorAlert(this.headerMsg, this.titleMsg, bodyMsg);
    }
    
    @Override
    public void warn(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = MessageFormat.format("{0} (WARN)", titleMsg);
        this.bodyMsg = bodyMsg;
        AppComponents.threadConfig().getTaskExecutor().execute(dbSendRun, 1);
    }
    
    @Override
    public void warn(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        warn(this.headerMsg, this.titleMsg, bodyMsg);
    }
    
    @Override
    public void warning(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        warn(this.headerMsg, this.titleMsg, bodyMsg);
    }
    
    @Override
    public void warning(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        warn(this.headerMsg, this.titleMsg, bodyMsg);
    }
    
    
}