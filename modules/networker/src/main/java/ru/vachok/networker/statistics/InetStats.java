package ru.vachok.networker.statistics;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.abstr.DataBaseRegSQL;
import ru.vachok.networker.accesscontrol.inetstats.InetStatSorter;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.awt.*;
import java.io.*;
import java.sql.*;
import java.time.LocalTime;
import java.util.List;


/**
 @since 20.05.2019 (9:36) */
public class InetStats implements Runnable, DataBaseRegSQL {
    
    
    private static final String FILENAME_INETSTATSIPCSV = "inetstatsIP.csv";
    
    private static final String FILENAME_INETSTATSCSV = "inetstats.csv";
    
    private static final String SQL_DISTINCTIPSWITHINET = "SELECT DISTINCT `ip` FROM `inetstats`";
    
    private StatsOfNetAndUsers statsOfNetAndUsers = new WeekStats();
    
    private String fileName;
    
    private String sql;
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    @Override public void run() {
        messageToUser.info(getClass().getSimpleName() + "in kbytes. ", new File(FILENAME_INETSTATSIPCSV).getAbsolutePath(), " = " + readInetStats());
        AppComponents.threadConfig().execByThreadConfig(new InetStatSorter());
    }
    
    @Override public void setSavepoint(Connection connection) {
        throw new IllegalComponentStateException("20.05.2019 (9:54)");
    }
    
    @Override public MysqlDataSource getDataSource() {
        return new RegRuMysql().getDataSource();
    }
    
    @Override public Savepoint getSavepoint(Connection connection) {
        throw new IllegalComponentStateException("20.05.2019 (9:55)");
    }
    
    @Override public int selectFrom() {
        try (Connection connection = getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
             PreparedStatement p = connection.prepareStatement(SQL_DISTINCTIPSWITHINET);
             ResultSet r = p.executeQuery();
             OutputStream outputStream = new FileOutputStream(fileName);
             PrintStream printStream = new PrintStream(outputStream, true)
        ) {
            if (sql.contains("SELECT * FROM `inetstats` WHERE `ip` LIKE")) {
                printStream.print(new java.util.Date(Long.parseLong(r.getString("Date"))));
                printStream.print(",");
                printStream.print(r.getString(ConstantsFor.DBFIELB_RESPONSE));
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
        catch (SQLException | IOException e) {
            messageToUser.error(e.getMessage());
        }
        return -1;
    }
    
    @Override public int insertTo() {
        throw new IllegalComponentStateException("20.05.2019 (10:03)");
    }
    
    @Override public int deleteFrom() {
        this.sql = "";
        try (Connection connection = getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
        
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage());
        }
        return -1;
    }
    
    @Override public int updateTable() {
        throw new IllegalComponentStateException("20.05.2019 (10:02)");
    }
    
    long readInetStats() {
        this.fileName = FILENAME_INETSTATSIPCSV;
        this.sql = SQL_DISTINCTIPSWITHINET;
        selectFrom();
        return new File(FILENAME_INETSTATSIPCSV).length() / ConstantsFor.KBYTE;
    }
    
    private void readInetStatsRSetToCSV() {
        List<String> chkIps = FileSystemWorker.readFileToList(new File(FILENAME_INETSTATSIPCSV).getPath());
        long totalBytes = 0;
        for (String ip : chkIps) {
            this.fileName = FILENAME_INETSTATSCSV.replace("inetstats", ip).replace(".csv", "_" + LocalTime.now().toSecondOfDay() + ".csv");
            File file = new File(fileName);
            this.sql = new StringBuilder().append("SELECT * FROM `inetstats` WHERE `ip` LIKE '").append(ip).append("'").toString();
            selectFrom();
            totalBytes += file.length();
            new MessageLocal(getClass().getSimpleName())
                .info(fileName, file.length() / ConstantsFor.KBYTE + " kb", "total kb: " + totalBytes / ConstantsFor.KBYTE);
            if (file.length() > 10) {
                this.sql = new StringBuilder().append("DELETE FROM `inetstats` WHERE `ip` LIKE '").append(ip).append("'").toString();
                selectFrom();
            }
        }
        new MessageSwing().infoTimer(ConstantsFor.ONE_DAY_HOURS * 3, "ALL STATS SAVED\n" + totalBytes / ConstantsFor.KBYTE + " Kbytes");
    }
    
    
}
