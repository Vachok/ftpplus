// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.DataBaseRegSQL;
import ru.vachok.networker.accesscontrol.inetstats.InetStatSorter;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.awt.*;
import java.io.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;


/**
 @since 20.05.2019 (9:36) */
public class InteretStats implements Runnable, DataBaseRegSQL {
    
    
    private static final String FILENAME_INETSTATSIPCSV = "inetstatsIP.csv";
    
    private static final String FILENAME_INETSTATSCSV = "inetstats.csv";
    
    private String fileName = "null";
    
    private Connection connectionF;
    
    private Savepoint savepoint;
    
    private String sql = "null";
    
    private static final String SQL_DISTINCTIPSWITHINET = ConstantsFor.SQL_SELECTINETSTATS;
    
    /**
     Для тестов
     <p>
     
     @return {@link #fileName}
     */
    protected String getFileName() {
        return fileName;
    }
    
    /**
     Для тестов
     <p>
     
     @param fileName имя csv-файла
     */
    protected void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    /**
     For tests
     
     @return {@link #sql}
     */
    protected String getSql() {
        return sql;
    }
    
    /**
     Для тестов
     <p>
     
     @param sql sql-запрос
     */
    protected void setSql(String sql) {
        this.sql = sql;
    }
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    @Override public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(FileSystemWorker.readFile(FILENAME_INETSTATSIPCSV));
        return stringBuilder.toString();
    }
    
    @Override public void run() {
        long iPsWithInet = readIPsWithInet();
        messageToUser.info(getClass().getSimpleName() + "in kbytes. ", new File(FILENAME_INETSTATSIPCSV).getAbsolutePath(), " = " + iPsWithInet);
        readInetStatsRSetToCSV();
        Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(new InetStatSorter());
    }
    
    @Override public int selectFrom() {
        try (Connection connection = getSavepointConnection()) {
            try (PreparedStatement p = connection.prepareStatement(sql)) {
                try (ResultSet r = p.executeQuery()) {
                    try (OutputStream outputStream = new FileOutputStream(fileName)) {
                        try (PrintStream printStream = new PrintStream(outputStream, true)) {
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
                    }
                }
            }
        }
        catch (SQLException | IOException e) {
            messageToUser.error(e.getMessage());
    
        }
        return -1;
    }
    
    @Override public int deleteFrom() {
        try (Connection connection = getSavepointConnection()) {
            try (PreparedStatement p = connection.prepareStatement(sql)) {
                return p.executeUpdate();
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage());
            releaseSavepoint();
        }
        return -1;
    }
    
    @Override public int insertTo() {
        throw new IllegalComponentStateException("20.05.2019 (10:03)");
    }
    
    private Connection getSavepointConnection() {
        MysqlDataSource sourceSchema = new RegRuMysql().getDataSourceSchema(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        sourceSchema.setRelaxAutoCommit(true);
        sourceSchema.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        try {
            this.connectionF = sourceSchema.getConnection();
            this.savepoint = connectionF.setSavepoint("befdel");
            return connectionF;
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage());
            System.err.println("Connection ***WITH SAVEPOINT*** to SQL cannot be established\nNO " + getClass().getSimpleName() + ".deleteFrom!");
            return new RegRuMysql().getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        }
    }
    
    private void releaseSavepoint() {
        try {
            String savepointName = savepoint.getSavepointName();
            connectionF.releaseSavepoint(savepoint);
            messageToUser.error(savepointName + " " + "released.");
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    @Override public int updateTable() {
        throw new IllegalComponentStateException("20.05.2019 (10:02)");
    }
    
    private long readIPsWithInet() {
        this.fileName = FILENAME_INETSTATSIPCSV;
        this.sql = SQL_DISTINCTIPSWITHINET;
        selectFrom();
        return new File(FILENAME_INETSTATSIPCSV).length() / ConstantsFor.KBYTE;
    }
    
    private void readInetStatsRSetToCSV() {
        List<String> chkIps = FileSystemWorker.readFileToList(new File(FILENAME_INETSTATSIPCSV).getPath());
        DateFormat format = new SimpleDateFormat("E");
        String weekDay = format.format(new Date());
        long totalBytes = 0;
        for (String ip : chkIps) {
            this.fileName = FILENAME_INETSTATSCSV.replace("inetstats", ip).replace(".csv", "_" + LocalTime.now().toSecondOfDay() + ".csv");
            File file = new File(fileName);
            this.sql = new StringBuilder().append("SELECT * FROM `inetstats` WHERE `ip` LIKE '").append(ip).append("'").toString();
            selectFrom();
            totalBytes += file.length();
            new MessageLocal(getClass().getSimpleName())
                .info(fileName, file.length() / ConstantsFor.KBYTE + " kb", "total kb: " + totalBytes / ConstantsFor.KBYTE);
            if (weekDay.equals("вс") && file.length() > 10) {
                this.sql = new StringBuilder().append("DELETE FROM `inetstats` WHERE `ip` LIKE '").append(ip).append("'").toString();
                System.out.println(deleteFrom() + " rows deleted.");
            }
        }
        new MessageSwing().infoTimer(ConstantsFor.ONE_DAY_HOURS * 3, "ALL STATS SAVED\n" + totalBytes / ConstantsFor.KBYTE + " Kbytes");
    }
}
