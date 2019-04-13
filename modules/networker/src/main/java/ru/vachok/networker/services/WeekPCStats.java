package ru.vachok.networker.services;



import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.abstr.DataBaseRegSQL;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.systray.MessageToTray;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 Сбор статы по-недельно
 <p>

 @since 08.12.2018 (0:12) */
public class WeekPCStats implements Runnable, DataBaseRegSQL {

    /**
     Лист только с именами ПК
     */
    private static final List<String> PC_NAMES_IN_TABLE = new ArrayList<>();

    private static MessageToUser messageToUser;

    private final String sql;

    private final String fileName;
    private IllegalStateException illegalStateException = new IllegalStateException("13.04.2019 (18:17)");


    public WeekPCStats(String sql , String fileName) {
        this.sql = sql;
        this.fileName = fileName;
    }


    public WeekPCStats(String sql) {
        this.sql = sql;
        this.fileName = ConstantsFor.FILENAME_VELKOMPCUSERAUTOTXT;
    }


    static {
        try {messageToUser = new MessageToTray(WeekPCStats.class.getSimpleName());} catch (UnsupportedOperationException e) {
            messageToUser = new MessageLocal(WeekPCStats.class.getSimpleName());
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setName("WeekPCStats.run");
        final long stArt = System.currentTimeMillis();
        selectFrom();
    }


    @Override public int selectFrom() {
        final long stArt = System.currentTimeMillis();
        int retInt = 0;
        File file = new File(fileName);
        try (Connection c = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery();
             OutputStream outputStream = new FileOutputStream(file);
             PrintWriter printWriter = new PrintWriter(outputStream, true)){
            while(r.next()){
                if(sql.equals(ConstantsFor.SQL_SELECTFROM_PCUSERAUTO)) pcUserAutoSelect(r , printWriter);
            }
            String msgTimeSp = new StringBuilder()
                .append("WeekPCStats.getFromDB method. ")
                .append(( float ) (System.currentTimeMillis() - stArt) / 1000)
                .append(" sec spend\n")
                .append(file.getAbsolutePath())
                .append(" ")
                .append(( float ) file.length() / ConstantsFor.KBYTE).toString();
            messageToUser.warn(msgTimeSp);
        }
        catch(SQLException | IOException e){
            messageToUser.infoNoTitles(e.getMessage());
        }
        String toCopy = "\\\\10.10.111.1\\Torrents-FTP\\" + file.getName();
        if(!ConstantsFor.thisPC().toLowerCase().contains("home")){
            toCopy = file.getName() + "_cp";
        }
        FileSystemWorker.copyOrDelFile(file, toCopy, false);
        return PC_NAMES_IN_TABLE.size();
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
        printWriter.println(new StringBuilder()
            .append(r.getString(1))
            .append(" at ")
            .append(r.getString(6))
            .append(") ")
            .append(r.getString(2))
            .append(" ")
            .append(r.getString(3)));
        PC_NAMES_IN_TABLE.add(r.getString(2) + " " + r.getString(3));
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WeekPCStats{");
        sb.append("PC_NAMES_IN_TABLE=").append(PC_NAMES_IN_TABLE);
        sb.append('}');
        return sb.toString();
    }
}