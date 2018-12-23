package ru.vachok.networker.services;


import org.slf4j.Logger;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.*;
import java.sql.*;
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
     Более 3х совпадений в строках
     */
    private static final String STR_SEC_SPEND = " sec spend";

    /**
     {@link #getFromDB()}
     */
    @Override
    public void run() {
        final long stArt = System.currentTimeMillis();
        getFromDB();
        String msgTimeSp = "WeekPCStats.run method. " + ( float ) (System.currentTimeMillis() - stArt) / 1000 + STR_SEC_SPEND;
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
        final long stArt = System.currentTimeMillis();
        String sql = "select * from pcuserauto";
        File file = new File("velkom_pcuserauto.txt");
        try(Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.DB_PREFIX + "velkom");
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
            LOGGER.warn(e.getMessage());
        }
        String toCopy = "\\\\10.10.111.1\\Torrents-FTP\\" + file.getName();
        if(!ConstantsFor.thisPC().toLowerCase().contains("home")){
            toCopy = file.getName() + "_cp";
        }
        FileSystemWorker.copyOrDelFile(file, toCopy, false);
    }
}