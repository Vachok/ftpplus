package ru.vachok.networker.services;


import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 Работа с календарём

 @since 09.12.2018 (15:26) */
public abstract class MyCalen {

    private MyCalen() {

    }

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
     Usage: {@link IntoApplication#runCommonScan()} <br> Uses: -
     <p>

     @param hourNeed час
     @param minNeed  минута
     @return new {@link Date} следующая суббота 0:01
     */
    public static Date getNextSat(int hourNeed, int minNeed) {
        final long stArt = System.currentTimeMillis();
        Calendar.Builder builder = new Calendar.Builder();
        LocalDate localDate = LocalDate.now();
        DayOfWeek satDay = DayOfWeek.SATURDAY;
        if (localDate.getDayOfWeek().toString().equalsIgnoreCase(satDay.toString())) {
            timeInfo.computeDetails();
            return new Date(timeInfo.getReturnTime() + TimeUnit.MINUTES.toMillis(14));
        } else {
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
                .append((float) (System.currentTimeMillis() - stArt) / 1000)
                .append(" sec spend\n")
                .append(retDate.toString())
                .append(" date returned").toString();
            LOGGER.info(msgTimeSp);
            return retDate;
        }
    }

    /**
     Создание {@link Date}
     <p>
     Usages: {@link IntoApplication#schedStarter()} <br> Uses: -

     @param hourNeed  час
     @param minNeed   минута
     @param dayOfWeek день недели
     @return нужный {@link Date}
     */
    public static Date getNextDayofWeek(int hourNeed, int minNeed, DayOfWeek dayOfWeek) {
        final long stArt = System.currentTimeMillis();
        Calendar.Builder cBuilder = new Calendar.Builder();
        LocalDate localDate = LocalDate.now();
        if (localDate.getDayOfWeek().equals(dayOfWeek)) {
            return new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(14));
        } else {
            int toDateDays = Math.abs(dayOfWeek.getValue() + 7 - localDate.getDayOfWeek().getValue());
            cBuilder
                .setDate(localDate.getYear(),
                    localDate.getMonthValue() - 1,
                    localDate.getDayOfMonth() + toDateDays).setTimeOfDay(hourNeed, minNeed, 0);
            timeInfo.computeDetails();
            long rDiff = System.currentTimeMillis() - timeInfo.getReturnTime();
            Date retDate = cBuilder.build().getTime();
            String msgTimeSp = new StringBuilder()
                .append("MyCalen.getNextDayofWeek method. ")
                .append((float) (System.currentTimeMillis() - stArt) / 1000)
                .append(" sec spend\n")
                .append(rDiff)
                .append(" System.currentTimeMillis()-timeInfo.getReturnTime()\n")
                .append(retDate.toString())
                .append(" date returned").toString();
            LOGGER.info(msgTimeSp);
            return retDate;
        }
    }

    /**
     // TODO: 19.12.2018 проверить метод!

     @return дата через месяц.
     */
    public static Date getNextMonth() {
        LocalDate localDate = LocalDate.now();
        Calendar.Builder builder = new Calendar.Builder();
        builder.setDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
        Calendar build = builder.build();
        build.setWeekDate(localDate.getYear(), build.getWeeksInWeekYear(), Calendar.SATURDAY);
        Instant instant = build.toInstant();
        Date date = new Date(instant.toEpochMilli());
        String msg = instant.toString() + " and ret date is: " + date.toString();
        LOGGER.warn(msg);
        return date;
    }
}