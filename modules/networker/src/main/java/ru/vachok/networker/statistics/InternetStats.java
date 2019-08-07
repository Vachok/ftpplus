// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.accesscontrol.inetstats.InetStatSorter;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.enums.UsefulUtilites;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;


/**
 @see InetStatSorter
 @since 20.05.2019 (9:36) */
public class InternetStats implements Runnable {
    
    
    private static final String SQL_DISTINCTIPSWITHINET = ConstantsFor.SQL_SELECTINETSTATS;
    
    private String fileName = "null";
    
    private String sql = "null";
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
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
        long iPsWithInet = readIPsWithInet();
        messageToUser
            .info(getClass().getSimpleName() + "in kbytes. ", new File(FileNames.FILENAME_INETSTATSIPCSV).getAbsolutePath(), " = " + iPsWithInet + " size in kb");
        
        if (weekDay.equals("вс")) {
            readStatsToCSVAndDeleteFromDB();
        }
        Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(new InetStatSorter());
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
    
    protected int selectFrom() {
        try (Connection connection = getSavepointConnection()) {
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
        catch (SQLException | IOException e) {
            messageToUser.error(e.getMessage());
    
        }
        return -1;
    }
    
    protected int deleteFrom() {
        try (Connection connection = getSavepointConnection()) {
            try (PreparedStatement p = connection.prepareStatement(sql)) {
                return p.executeUpdate();
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage());
        }
        return -1;
    }
    
    private void printToFile(@NotNull ResultSet r, PrintStream printStream) throws SQLException {
        while (r.next()) {
            if (sql.contains("SELECT * FROM `inetstats` WHERE `ip` LIKE")) {
                printStream.print(new java.util.Date(Long.parseLong(r.getString("Date"))));
                printStream.print(",");
                printStream.print(r.getString(ConstantsFor.DBFIELD_RESPONSE));
                printStream.print(",");
                printStream.print(r.getString("bytes"));
                printStream.print(",");
                printStream.print(r.getString(ConstantsFor.DBFIELD_METHOD));
                printStream.print(",");
                printStream.print(r.getString("site"));
                printStream.println();
            }
            if (sql.equals(SQL_DISTINCTIPSWITHINET)) {
                printStream.println(r.getString("ip"));
            }
        }
    }
    
    private Connection getSavepointConnection() {
        MysqlDataSource sourceSchema = new RegRuMysql().getDataSourceSchema(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        sourceSchema.setRelaxAutoCommit(true);
        sourceSchema.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        try (Connection connectionF = sourceSchema.getConnection()) {
            return connectionF;
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage());
            System.err.println("Connection ***WITH SAVEPOINT*** to SQL cannot be established\nNO " + getClass().getSimpleName() + ".deleteFrom!");
            return new RegRuMysql().getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        }
    }
    
    private long readIPsWithInet() {
        this.fileName = FileNames.FILENAME_INETSTATSIPCSV;
        this.sql = SQL_DISTINCTIPSWITHINET;
        selectFrom();
        return new File(FileNames.FILENAME_INETSTATSIPCSV).length() / ConstantsFor.KBYTE;
    }
    
    private void readStatsToCSVAndDeleteFromDB() {
        List<String> chkIps = FileSystemWorker.readFileToList(new File(FileNames.FILENAME_INETSTATSIPCSV).getPath());
        long totalBytes = 0;
        for (String ip : chkIps) {
            this.fileName = FileNames.FILENAME_INETSTATSCSV.replace(ConstantsFor.STR_INETSTATS, ip)
                .replace(".csv", "_" + LocalTime.now().toSecondOfDay() + ".csv");
            File file = new File(fileName);
            this.sql = new StringBuilder().append("SELECT * FROM `inetstats` WHERE `ip` LIKE '").append(ip).append("'").toString();
            selectFrom();
            totalBytes += file.length();
            messageToUser.info(fileName, file.length() / ConstantsFor.KBYTE + " kb", "total kb: " + totalBytes / ConstantsFor.KBYTE);
            if (file.length() > 10) {
                this.sql = new StringBuilder().append("DELETE FROM `inetstats` WHERE `ip` LIKE '").append(ip).append("'").toString();
                System.out.println(deleteFrom() + " rows deleted.");
            }
        }
        new MessageSwing().infoTimer(UsefulUtilites.ONE_DAY_HOURS * 3, "ALL STATS SAVED\n" + totalBytes / ConstantsFor.KBYTE + " Kbytes");
    }
}
