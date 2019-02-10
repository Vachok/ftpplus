package ru.vachok.networker.net;


import org.slf4j.Logger;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;

import java.io.*;
import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


/**
 Сбор статы по-недельно
 <p>

 @since 08.12.2018 (0:12) */
public class WeekPCStats implements Runnable {

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     Лист только с именами ПК
     */
    private static final List<String> PC_NAMES_IN_TABLE = new ArrayList<>();

    /**
     {@link #getFromDB()}
     */
    @Override
    public void run() {
        Thread.currentThread().setName("WeekPCStats.run");
        LOGGER.warn("WeekPCStats.run");
        final long stArt = System.currentTimeMillis();
        getFromDB();
        String tSpend = ConstantsFor.STR_SEC_SPEND;
        String msgTimeSp = MessageFormat
            .format("WeekPCStats.run method. {0}{1}", ( float ) (System.currentTimeMillis() - stArt) / 1000, tSpend);
        LOGGER.info(msgTimeSp);
    }

    /**
     Инфо из БД
     <p>
     Читает БД pcuserauto <br>
     Записывает файл {@code velkom_pcuserauto.txt} <br>
     Usages: {@link #run()}
     */
    private void getFromDB() {
        MessageToUser messageToUser = new MessageSwing();
        final long stArt = System.currentTimeMillis();
        String sql = "select * from pcuserauto";
        File file = new File(ConstantsNet.VELKOM_PCUSERAUTO_TXT);
        try(Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.DB_PREFIX + ConstantsFor.STR_VELKOM);
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
                    .append(r.getString(3)).toString());
                PC_NAMES_IN_TABLE.add(r.getString(2) + " " + r.getString(3));
            }
            String msgTimeSp = new StringBuilder()
                .append("WeekPCStats.getFromDB method. ")
                .append(( float ) (System.currentTimeMillis() - stArt) / 1000)
                .append(" sec spend\n")
                .append(file.getAbsolutePath())
                .append(" ")
                .append(( float ) file.length() / ConstantsFor.KBYTE).toString();
            LOGGER.warn(msgTimeSp);
        }
        catch(SQLException | IOException e){
            messageToUser.infoNoTitles(e.getMessage());
        }
        String toCopy = "\\\\10.10.111.1\\Torrents-FTP\\" + file.getName();
        if(!ConstantsFor.thisPC().toLowerCase().contains("home")){
            toCopy = file.getName() + "_cp";
        }
        FileSystemWorker.copyOrDelFile(file, toCopy, true);
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