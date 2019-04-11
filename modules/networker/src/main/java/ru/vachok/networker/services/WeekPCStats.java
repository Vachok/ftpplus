package ru.vachok.networker.services;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.systray.MessageToTray;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


/**
 Сбор статы по-недельно
 <p>

 @since 08.12.2018 (0:12) */
public class WeekPCStats implements Runnable {

    /**
     Лист только с именами ПК
     */
    private static final List<String> PC_NAMES_IN_TABLE = new ArrayList<>();

    private static MessageToUser messageToUser;

    static {
        try {messageToUser = new MessageToTray(WeekPCStats.class.getSimpleName());} catch (UnsupportedOperationException e) {
            messageToUser = new MessageLocal(WeekPCStats.class.getSimpleName());
        }
    }
    
    
    /**
     {@link #getFromDB()}
     */
    @Override
    public void run() {
        Thread.currentThread().setName("WeekPCStats.run");
        final long stArt = System.currentTimeMillis();
        getFromDB();
        String tSpend = ConstantsFor.STR_SEC_SPEND;
        String msgTimeSp = MessageFormat
            .format("WeekPCStats.run method. {0}{1}", ( float ) (System.currentTimeMillis() - stArt) / 1000, tSpend);
        messageToUser.info(msgTimeSp);
    }

    /**
     Инфо из БД
     <p>
     Читает БД pcuserauto <br>
     Записывает файл {@code velkom_pcuserauto.txt} <br>
     Usages: {@link #run()}
     */
    private void getFromDB() {
        final long stArt = System.currentTimeMillis();
        String sql = "select * from pcuserauto";
        File file = new File(ConstantsNet.VELKOM_PCUSERAUTO_TXT);
        try (Connection c = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery();
             OutputStream outputStream = new FileOutputStream(file);
             PrintWriter printWriter = new PrintWriter(outputStream, true)){
            while(r.next()){
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
        messageToUser.infoNoTitles(this.getClass().getSimpleName() + " ends\n" + PC_NAMES_IN_TABLE.size() + " PC_NAMES_IN_TABLE.size()");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WeekPCStats{");
        sb.append("PC_NAMES_IN_TABLE=").append(PC_NAMES_IN_TABLE);
        sb.append('}');
        return sb.toString();
    }
}