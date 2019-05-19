// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.DataBaseRegSQL;
import ru.vachok.networker.accesscontrol.inetstats.InetStatSorter;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.systray.MessageToTray;

import java.io.*;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


/**
 Сбор статы по-недельно
 <p>
 Устойчивость (in/(in+out)): 2/(2+6) = 0.25 (устойчив на 75%);
 @since 08.12.2018 (0:12) */
public class WeekPCStats implements Runnable, DataBaseRegSQL, StatsOfNetAndUsers {
    
    /**
     Лист только с именами ПК
     */
    private static final List<String> PC_NAMES_IN_TABLE = new ArrayList<>();

    private static final String FILENAME_INETSTATSIPCSV = "inetstatsIP.csv";

    private static final String FILENAME_INETSTATSCSV = "inetstats.csv";
    
    private static MessageToUser messageToUser;

    private String sql;

    private String fileName;
    
    private IllegalStateException illegalStateException = new IllegalStateException("13.04.2019 (18:17)");

    private static final String SQL_DISTINCTIPSWITHINET = "SELECT DISTINCT `ip` FROM `inetstats`";
    
    
    public WeekPCStats(String sql, String fileName) {
        this.sql = sql;
        this.fileName = fileName;
    }


    public WeekPCStats(String sql) {
        this.sql = sql;
        this.fileName = ConstantsFor.FILENAME_VELKOMPCUSERAUTOTXT;
    }
    
    public WeekPCStats() {
        this.sql = ConstantsFor.SQL_SELECTFROM_PCUSERAUTO;
    }
    
    
    static {
        try {
            messageToUser = new MessageToTray(WeekPCStats.class.getSimpleName());
        }
        catch (UnsupportedOperationException e) {
            messageToUser = new MessageLocal(WeekPCStats.class.getSimpleName());
        }
    }
    
    
    @Override public String getStats() {
        this.fileName = ConstantsFor.FILENAME_VELKOMPCUSERAUTOTXT;
        selectFrom();
        return new TForms().fromArray(PC_NAMES_IN_TABLE, false);
    }
    
    @Override
    public void run() {
        AppComponents.threadConfig().thrNameSet("week");
        final long stArt = System.currentTimeMillis();
        pcUsrAutoMake();
        messageToUser.info(getClass().getSimpleName() + "in kbytes. " , new File(FILENAME_INETSTATSIPCSV).getAbsolutePath() , " = " + readInetStats());
        readInetStatsRSetToCSV();
        AppComponents.threadConfig().execByThreadConfig(new InetStatSorter());
    }


    @Override public int selectFrom() {
        final long stArt = System.currentTimeMillis();
        int retInt = 0;
        File file = new File(fileName);
        try(Connection c = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)){
            try(PreparedStatement p = c.prepareStatement(sql)){
                if(sql.contains("DELETE")) {
                    int delUp = p.executeUpdate();
                    new MessageLocal(getClass().getSimpleName()).info(fileName , "deleted" , delUp + " rows.");
                    return delUp;
                }
                try(ResultSet r = p.executeQuery()){
                    try(OutputStream outputStream = new FileOutputStream(file)){
                        try (PrintStream printStream = new PrintStream(outputStream, true)) {
                            while(r.next()){
                                if (sql.equals(ConstantsFor.SQL_SELECTFROM_PCUSERAUTO)) {
                                    pcUserAutoSelect(r, printStream);
                                }
                                if(sql.equals(SQL_DISTINCTIPSWITHINET)) {
                                    printStream.println(r.getString("ip"));
                                }
                                if(sql.contains("SELECT * FROM `inetstats` WHERE `ip` LIKE")) {
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
                            }
                        }
                    }
                }
            }
        }catch(SQLException | IOException e){
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".selectFrom", e));
        }
        String toCopy = "\\\\10.10.111.1\\Torrents-FTP\\" + file.getName();
        if(!ConstantsFor.thisPC().toLowerCase().contains("home")){
            toCopy = file.getName() + "_cp";
        }
        FileSystemWorker.copyOrDelFile(file, toCopy, false);
        file.deleteOnExit();
        return PC_NAMES_IN_TABLE.size();
    }


    private void pcUsrAutoMake() {
        messageToUser.info(getClass().getSimpleName() + ".pcUsrAutoMake", "pcUsrAutoMake();", " = " + selectFrom());
    }


    private void readInetStatsRSetToCSV() {
        List<String> chkIps = FileSystemWorker.readFileToList(new File(FILENAME_INETSTATSIPCSV).getPath());
        long totalBytes = 0;
        for(String ip : chkIps){
            this.fileName = FILENAME_INETSTATSCSV.replace("inetstats" , ip).replace(".csv" , "_" + LocalTime.now().toSecondOfDay() + ".csv");
            File file = new File(fileName);
            this.sql = "SELECT * FROM `inetstats` WHERE `ip` LIKE '" + ip + "'";
            selectFrom();
            totalBytes = totalBytes + file.length();
            new MessageLocal(getClass().getSimpleName())
                .info(fileName, file.length() / ConstantsFor.KBYTE + " kb", "total kb: " + totalBytes / ConstantsFor.KBYTE);
            if(file.length() > 10) {
                this.sql = "DELETE FROM `inetstats` WHERE `ip` LIKE '" + ip + "'";
                selectFrom();
            }
        }
        new MessageSwing().infoTimer(ConstantsFor.ONE_DAY_HOURS * 3, "ALL STATS SAVED\n" + totalBytes / ConstantsFor.KBYTE + " Kbytes");
    }


    private long readInetStats() {
        this.fileName = FILENAME_INETSTATSIPCSV;
        this.sql = SQL_DISTINCTIPSWITHINET;
        selectFrom();
        return new File(FILENAME_INETSTATSIPCSV).length() / ConstantsFor.KBYTE;
    }


    @Override public int insertTo() {
        throw illegalStateException;
    }


    @Override public int deleteFrom() {
        throw illegalStateException;
    }


    @Override public int updateTable() {
        throw illegalStateException;
    }


    @Override public void setSavepoint(Connection connection) {
        throw illegalStateException;
    }


    @Override public MysqlDataSource getDataSource() {
        throw illegalStateException;
    }


    @Override public Savepoint getSavepoint(Connection connection) {
        throw illegalStateException;
    }
    
    
    private void pcUserAutoSelect(ResultSet r, PrintStream prStream) throws SQLException {
        String toPrint = r.getString(2) + " " + r.getString(3);
        PC_NAMES_IN_TABLE.add(toPrint);
        prStream.println(toPrint);
    }
    
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WeekPCStats{");
        sb.append("PC_NAMES_IN_TABLE=").append(PC_NAMES_IN_TABLE);
        sb.append('}');
        return sb.toString();
    }
}
