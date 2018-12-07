package ru.vachok.networker.services;


import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.*;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;


/**
 Сбор статы по-недельно
 <p>

 @since 08.12.2018 (0:12) */
public class WeekPCStats implements Runnable {

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    @Override
    public void run() {
        final long stArt = System.currentTimeMillis();
        TimeInfo nowAtomTInfo = new TimeChecker().call();
        nowAtomTInfo.computeDetails();
        Date trueDate = new Date(nowAtomTInfo.getReturnTime());
        DayOfWeek sunDay = DayOfWeek.SUNDAY;
        LocalDateTime localDate = LocalDateTime.ofInstant(trueDate.toInstant(), ZoneId.systemDefault());
        String msg = localDate.toString() + "\nisSunday? " + localDate.getDayOfWeek().equals(sunDay) + "\nNow hour is " + localDate.getHour();
        LOGGER.info(msg);
        if(localDate.getDayOfWeek().equals(sunDay) && localDate.getHour() > 21){
            getFromDB();
        }
        else{
            Thread.currentThread().interrupt();
        }
        String msgTimeSp = "WeekPCStats.run method. " + ( float ) (System.currentTimeMillis() - stArt) / 1000 + " sec spend";
        LOGGER.info(msgTimeSp);
    }

    private void getFromDB() {
        String sql = "select * from pcuserauto";
        try(Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.DB_PREFIX + "velkom");
            PreparedStatement p = c.prepareStatement(sql);
            ResultSet r = p.executeQuery();
            OutputStream outputStream = new FileOutputStream(System.currentTimeMillis() + "_pcuserauto.txt");
            PrintWriter printWriter = new PrintWriter(outputStream, true)){
            while(r.next()){
                printWriter.println(r.getString(1) + " at " + r.getString(6) + ")");
                printWriter.print(r.getString(2) + ",");
                printWriter.print(r.getString(3) + ",");
            }
        }
        catch(SQLException | IOException e){
            LOGGER.warn(e.getMessage());
        }
    }
}