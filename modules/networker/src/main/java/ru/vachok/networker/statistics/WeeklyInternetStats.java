// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.inetstats.InetStatSorter;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;


/**
 @see InetStatSorter
 @since 20.05.2019 (9:36) */
public class WeeklyInternetStats extends Stats implements Runnable {
    
    
    private static final String SQL_DISTINCTIPSWITHINET = ConstantsFor.SQL_SELECTINETSTATS;
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    @Override
    public void setClassOption(@NotNull Object classOption) {
    
    }
    
    @Override
    public String getInfo() {
        run();
        return toString();
    }
    
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(FileSystemWorker.readFile(FileNames.FILENAME_INETSTATSIPCSV));
        return stringBuilder.toString();
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
        messageToUser
                .info(getClass().getSimpleName() + "in kbytes. ", new File(FileNames.FILENAME_INETSTATSIPCSV).getAbsolutePath(), " = " + iPsWithInet + " size in kb");
        
        if (weekDay.equals("вс")) {
            readStatsToCSVAndDeleteFromDB();
        }
        Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(new InetStatSorter());
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
        long totalBytes = 0;
        for (String ip : chkIps) {
            this.fileName = FileNames.FILENAME_INETSTATSCSV.replace(ConstantsFor.STR_INETSTATS, ip)
                    .replace(".csv", "_" + LocalTime.now().toSecondOfDay() + ".csv");
            File file = new File(fileName);
            String limitSQL = "' LIMIT 500000";
            this.sql = new StringBuilder().append("SELECT * FROM `inetstats` WHERE `ip` LIKE '").append(ip).append(limitSQL).toString();
            selectFrom();
            totalBytes += file.length();
            messageToUser.info(fileName, file.length() / ConstantsFor.KBYTE + " kb", "total kb: " + totalBytes / ConstantsFor.KBYTE);
            if (file.length() > 10) {
                this.sql = new StringBuilder().append("DELETE FROM `inetstats` WHERE `ip` LIKE '").append(ip).append(limitSQL).toString();
                System.out.println(deleteFrom() + " rows deleted.");
            }
        }
        new MessageSwing().infoTimer(10, "ALL STATS SAVED\n" + totalBytes / ConstantsFor.KBYTE + " Kbytes");
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
    
    protected int selectFrom() {
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
        }
        catch (SQLException | IOException | OutOfMemoryError e) {
            messageToUser.error(e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
            Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(new WeeklyInternetStats());
        }
        return -1;
    }
    
    protected long deleteFrom() {
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
    
    protected String getSql() {
        return sql;
    }
    
    protected void setSql() {
        this.sql = ConstantsFor.SQL_SELECTINETSTATS;
    }
}
