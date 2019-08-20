// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.inetstats.InetStatSorter;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.Executors;


/**
 @see InetStatSorter
 @since 20.05.2019 (9:36) */
public class WeeklyInternetStats extends Stats implements Runnable {
    
    
    private static final String SQL_DISTINCTIPSWITHINET = ConstantsFor.SQL_SELECTINETSTATS;
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    private long totalBytes = 0;
    
    @Override
    public void setClassOption(@NotNull Object classOption) {
        this.messageToUser = (MessageToUser) classOption;
    }
    
    @Override
    public String getInfo() {
        if (getCurrentDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            run();
        }
        return toString();
    }
    
    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        DateFormat format = new SimpleDateFormat("E");
        String weekDay = format.format(new Date());
        long iPsWithInet = 0;
        try {
            iPsWithInet = readIPsWithInet();
        }
        catch (RuntimeException e) {
            messageToUser
                    .error(MessageFormat.format("InternetStats.run {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
    
        String headerMsg = MessageFormat.format("{0} in kb. ", getClass().getSimpleName());
        String titleMsg = new File(FileNames.FILENAME_INETSTATSIPCSV).getAbsolutePath();
        String bodyMsg = " = " + iPsWithInet + " size in kb";
    
        messageToUser.info(headerMsg, titleMsg, bodyMsg);
    
        if (getCurrentDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            readStatsToCSVAndDeleteFromDB();
            Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(new InetStatSorter());
        }
        else {
            throw new InvokeIllegalException(LocalDate.now().getDayOfWeek().name() + " not best day for stats...");
        }
        
    }
    
    private long readIPsWithInet() {
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_DISTINCTIPSWITHINET)) {
                try (ResultSet r = preparedStatement.executeQuery()) {
                    makeIPFile(r);
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat
                    .format("InternetStats.readIPsWithInet {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        return new File(FileNames.FILENAME_INETSTATSIPCSV).length() / ConstantsFor.KBYTE;
    }
    
    private void readStatsToCSVAndDeleteFromDB() {
        List<String> chkIps = FileSystemWorker.readFileToList(new File(FileNames.FILENAME_INETSTATSIPCSV).getPath());
        for (String ip : chkIps) {
            messageToUser.info(writeLog(ip, "300000"));
        }
        new MessageSwing().infoTimer(10, "ALL STATS SAVED\n" + totalBytes / ConstantsFor.KBYTE + " Kb");
    }
    
    private void makeIPFile(@NotNull ResultSet r) throws SQLException {
        try (OutputStream outputStream = new FileOutputStream(FileNames.FILENAME_INETSTATSIPCSV)) {
            try (PrintStream printStream = new PrintStream(outputStream, true)) {
                while (r.next()) {
                    printStream.println(r.getString("ip"));
                }
            }
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        
    }
    
    @Override
    public String writeLog(String ip, String rowsLimit) {
        this.fileName = ip + "_" + LocalTime.now().toSecondOfDay() + ".csv";
        this.sql = new StringBuilder().append("SELECT * FROM `inetstats` WHERE `ip` LIKE '").append(ip).append("' LIMIT ").append(rowsLimit).toString();
        String retStr = downloadConcreteIPStatistics();
        File file = new File(fileName);
        this.totalBytes += file.length();
        
        retStr = MessageFormat.format("{0} file is {1}. Total kb: {2}", retStr, file.length() / ConstantsFor.KBYTE, totalBytes / ConstantsFor.KBYTE);
        
        if (getCurrentDayOfWeek().equals(DayOfWeek.SUNDAY) & file.length() > 10) {
            retStr = MessageFormat.format("{0} ||| {1} rows deleted.", retStr, deleteFrom(ip, rowsLimit));
        }
        return retStr;
    }
    
    private String downloadConcreteIPStatistics() {
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement p = connection.prepareStatement(sql)) {
                try (ResultSet r = p.executeQuery()) {
                    try (OutputStream outputStream = new FileOutputStream(fileName)) {
                        try (PrintStream printStream = new PrintStream(outputStream, true)) {
                            printToFile(r, printStream);
                        }
                    }
                }
            }
            return fileName;
        }
        catch (SQLException | IOException | OutOfMemoryError e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
            Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(new WeeklyInternetStats());
            return e.getMessage();
        }
    }
    
    protected long deleteFrom(String ip, String rowsLimit) {
        this.sql = new StringBuilder().append("DELETE FROM `inetstats` WHERE `ip` LIKE '").append(ip).append("' LIMIT ").append(rowsLimit).toString();
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement p = connection.prepareStatement(sql)) {
                return p.executeLargeUpdate();
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
            Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(new WeeklyInternetStats());
        }
        return -1;
    }
    
    private void printToFile(@NotNull ResultSet r, PrintStream printStream) throws SQLException {
        while (r.next()) {
            printStream.print(new java.util.Date(Long.parseLong(r.getString("Date"))));
            printStream.print(",");
            printStream.print(r.getString(ConstantsFor.DBFIELD_RESPONSE));
            printStream.print(",");
            printStream.print(r.getString(ConstantsFor.SQLCOL_BYTES));
            printStream.print(",");
            printStream.print(r.getString(ConstantsFor.DBFIELD_METHOD));
            printStream.print(",");
            printStream.print(r.getString("site"));
            printStream.println();
        }
    }
    
    protected String getFileName() {
        return fileName;
    }
    
    protected void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    protected void setSql() {
        this.sql = ConstantsFor.SQL_SELECTINETSTATS;
    }
    
    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(",\n", WeeklyInternetStats.class.getSimpleName() + "[\n", "\n]");
        stringJoiner.add("totalBytes = " + totalBytes);
        if (!getCurrentDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            stringJoiner.add(LocalDate.now().getDayOfWeek().name());
        }
        return stringJoiner.toString();
    }
}
