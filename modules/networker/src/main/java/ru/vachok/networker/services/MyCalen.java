package ru.vachok.networker.services;


import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;


/**
 Работа с календарём

 @since 09.12.2018 (15:26) */
public abstract class MyCalen {

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     {@link TimeChecker}
     */
    private static final TimeChecker TIME_CHECKER = new TimeChecker();

    /**
     {@link TimeChecker#call()}
     */
    private static TimeInfo timeInfo = TIME_CHECKER.call();

    /**
     Дата запуска common scanner
     <p>
     Usage: {@link IntoApplication#runCommonScan()} <br>
     Uses: - <br>

     @return new {@link Date} следующая суббота 0:01
     */
    public static Date getNextSat(int hourNeed, int minNeed) {
        final long stArt = System.currentTimeMillis();
        Calendar.Builder builder = new Calendar.Builder();
        LocalDate localDate = LocalDate.now();
        DayOfWeek satDay = DayOfWeek.SATURDAY;
        if(localDate.getDayOfWeek().toString().equalsIgnoreCase(satDay.toString())){
            return new Date();
        }
        else{
            int toSat = satDay.getValue() - localDate.getDayOfWeek().getValue();
            Date retDate = builder
                .setDate(
                    localDate.getYear(),
                    localDate.getMonth().getValue() - 1,
                    localDate.getDayOfMonth() + toSat)
                .setTimeOfDay(hourNeed, minNeed, 0).build().getTime();
            timeInfo.computeDetails();

            String msgTimeSp = new StringBuilder()
                .append(retDate.toString())
                .append(" ")
                .append(toSat)
                .append("\nTimeChecker information: ")
                .append(timeInfo.getMessage())
                .append("\nMyCalen.getNextSat method. ")
                .append(( float ) (System.currentTimeMillis() - stArt) / 1000)
                .append(" sec spend\n")
                .append(retDate.toString())
                .append(" date returned").toString();
            LOGGER.info(msgTimeSp);
            return retDate;
        }
    }

    public static Date getNextDayofWeek(int hourNeed, int minNeed, DayOfWeek dayOfWeek) {
        final long stArt = System.currentTimeMillis();
        Calendar.Builder cBuilder = new Calendar.Builder();
        LocalDateTime localDateTime = LocalDateTime.now();
        if(localDateTime.getDayOfWeek().toString().equalsIgnoreCase(dayOfWeek.toString())){
            return new Date();
        }
        else{
            int toDateDays = dayOfWeek.getValue() - localDateTime.getDayOfWeek().getValue();
            cBuilder
                .setDate(localDateTime.getYear(),
                    localDateTime.getMonthValue() - 1,
                    localDateTime.getDayOfMonth() + toDateDays).setTimeOfDay(hourNeed, minNeed, 0);
            timeInfo.computeDetails();
            long rDiff = System.currentTimeMillis() - timeInfo.getReturnTime();
            Date retDate = cBuilder.build().getTime();
            String msgTimeSp = new StringBuilder()
                .append("MyCalen.getNextDayofWeek method. ")
                .append(( float ) (System.currentTimeMillis() - stArt) / 1000)
                .append(" sec spend\n")
                .append(rDiff)
                .append(" System.currentTimeMillis()-timeInfo.getReturnTime()\n")
                .append(retDate.toString())
                .append(" date returned").toString();
            LOGGER.info(msgTimeSp);
            return retDate;
        }
    }
}