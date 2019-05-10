// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.DataBaseRegSQL;
import ru.vachok.networker.accesscontrol.inetstats.InetStatSorter;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.systray.MessageToTray;

import java.io.*;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 Сбор статы по-недельно
 <p>
 Устойчивость (in/(in+out)): 2/(2+6) = 0.25 (устойчив на 75%);
 @since 08.12.2018 (0:12) */
public class WeekPCStats implements Runnable, DataBaseRegSQL {

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

    public WeekPCStats(String sql) {
        this.sql = sql;
        this.fileName = ConstantsFor.FILENAME_VELKOMPCUSERAUTOTXT;
    }
    
    static {
        try {
            messageToUser = new MessageToTray(WeekPCStats.class.getSimpleName());
        }
        catch (UnsupportedOperationException e) {
            messageToUser = new MessageLocal(WeekPCStats.class.getSimpleName());
        }
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
        try(Connection c = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)){
            try(PreparedStatement p = c.prepareStatement(sql)){
                if (sql.contains("DELETE")) {
                    contDelete(p);
                }
                
                try(ResultSet r = p.executeQuery()){
                    try (OutputStream outputStream = new FileOutputStream(fileName)) {
                        try(PrintWriter printWriter = new PrintWriter(outputStream , true)){
                            printWriter.println(r.getFetchSize());
                            printWriter.print(" FetchSize of ResultSet r. ");
                            printWriter.print(new java.util.Date());
                            printWriter.println();
                            while(r.next()){
                                if (sql.equals(ConstantsFor.SQL_SELECTFROM_PCUSERAUTO)) {
                                    pcUserAutoSelect(r, printWriter);
                                }
                                if(sql.equals(SQL_DISTINCTIPSWITHINET)) {
                                    printWriter.println(r.getString("ip"));
                                }
                                if(sql.contains("SELECT * FROM `inetstats` WHERE `ip` LIKE")) {
                                    inetstatsIP(printWriter, r);
                                }
                            }
                        }
                    }
                }
            }
        }catch(SQLException | IOException e){
            messageToUser.error(new TForms().fromArray(e, false));
        }
        String toCopy = "\\\\10.10.111.1\\Torrents-FTP\\" + fileName;
        if(!ConstantsFor.thisPC().toLowerCase().contains("home")){
            toCopy = fileName + "_cp";
        }
        File file = new File(fileName);
        boolean isCopyDel = FileSystemWorker.copyOrDelFile(file, toCopy, false);
        if (isCopyDel) {
            boolean isDelete = file.delete();
            if (!isDelete) {
                file.deleteOnExit();
            }
        }
        return PC_NAMES_IN_TABLE.size();
    }
    
    private void inetstatsIP(PrintWriter printWriter, ResultSet r) throws SQLException {
        printWriter.print(new java.util.Date(Long.parseLong(r.getString("Date"))));
        printWriter.print(",");
        printWriter.print(r.getString(ConstantsFor.DBFIELB_RESPONSE));
        printWriter.print(",");
        printWriter.print(r.getString("bytes"));
        printWriter.print(",");
        printWriter.print(r.getString(ConstantsFor.DBFIELD_METHOD));
        printWriter.print(",");
        printWriter.print(r.getString("site"));
        printWriter.println();
    }
    
    private void contDelete(PreparedStatement p) throws SQLException {
        int delUp = p.executeUpdate();
        messageToUser.info(fileName, "deleted", delUp + " rows.");
    }
    
    private void pcUsrAutoMake() {
        messageToUser.warn(getClass().getSimpleName(), fileName + "file, SQL: " + sql, " = " + selectFrom());
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
                .info(fileName , file.length() / ConstantsFor.KBYTE + " kb" , "total megabytes: " + totalBytes / ConstantsFor.KBYTE / ConstantsFor.KBYTE);
            if(file.length() > 10) {
                this.sql = "DELETE FROM `inetstats` WHERE `ip` LIKE '" + ip + "'";
                selectFrom();
            }
        }
        new MessageSwing().infoTimer(ConstantsFor.ONE_DAY_HOURS * 3 , "ALL STATS SAVED\n" + totalBytes / ConstantsFor.KBYTE / ConstantsFor.KBYTE + " Megabytes");
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
    
    
    private void pcUserAutoSelect(ResultSet r , PrintWriter printWriter) throws SQLException {
        printWriter.println(r.getString(1));
        printWriter.print(" at ");
        printWriter.print(r.getString(6));
        printWriter.print(") ");
        printWriter.print(r.getString(2));
        printWriter.print(" ");
        printWriter.print(r.getString(3));
        PC_NAMES_IN_TABLE.add(r.getString(2) + " " + r.getString(3));
        filesWork();
    }
    
    private void filesWork() {
        File file = new File(fileName);
        Set<String> usersUnique = FileSystemWorker.readFileToSet(file.toPath());
        messageToUser.info(getClass().getSimpleName() + ".filesWork", "usersUnique.size()", " = " + usersUnique.size());
        FileSystemWorker.writeFile(ConstantsFor.FILENAME_USERSSET, usersUnique.stream());
    }
    
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WeekPCStats{");
        sb.append("PC_NAMES_IN_TABLE=").append(PC_NAMES_IN_TABLE);
        sb.append('}');
        return sb.toString();
    }
}
