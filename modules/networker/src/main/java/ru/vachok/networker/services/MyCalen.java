package ru.vachok.networker.services;


import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 Работа с календарём

 @since 09.12.2018 (15:26) */
public abstract class MyCalen {

    private static final String DATE_RETURNED = " date returned";

    private MyCalen() {

    }

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger(MyCalen.class.getSimpleName());

    /**
     {@link TimeChecker}
     */
    private static final TimeChecker TIME_CHECKER = new TimeChecker();

    /**
     @return {@link TimeInfo}
     */
    public static TimeInfo getTimeInfo() {
        return timeInfo;
    }

    /**
     {@link TimeChecker#call()}
     */
    private static TimeInfo timeInfo = TIME_CHECKER.call();

    /**
     Проверка работоспособности.
     <p>
     {@code getNextDayofWeek(0, 4, DayOfWeek.FRIDAY; getNextMonth(); getNextSat(0, 2); getNextDayofWeek(10, 2, DayOfWeek.MONDAY)}

     @return результаты методов.
     */
    public static String toStringS() {
        final StringBuilder sb = new StringBuilder("MyCalen\n");
        sb.append(" getNextDayofWeek (FRI) = ").append(getNextDayofWeek(0, 4, DayOfWeek.FRIDAY)).append("\n");
        sb.append(", getNextMonth = ").append(getNextMonth()).append("\n");
        sb.append(", getNextSat = ").append(getNextSat(0, 2)).append("\n");
        sb.append(",  IS = ").append(getNextDayofWeek(10, 2, DayOfWeek.MONDAY)).append("\n");
        return sb.toString();
    }

    /**
     Создание {@link Date}
     <p>
     След. день недели.
     <p>
     @param hourNeed  час
     @param minNeed   минута
     @param dayOfWeek день недели
     @return нужный {@link Date}
     */
    public static Date getNextDayofWeek(int hourNeed, int minNeed, DayOfWeek dayOfWeek) {
        final long stArt = System.currentTimeMillis();
        Calendar.Builder cBuilder = new Calendar.Builder();
        LocalDate localDate = LocalDate.now();
        int toDate = dayOfWeek.getValue();
        if(dayOfWeek.equals(DayOfWeek.MONDAY)){
            toDate = dayOfWeek.getValue() + 7;
        }
        if(localDate.getDayOfWeek().equals(dayOfWeek) && LocalTime.now().isBefore(LocalTime.parse(String.format("%02d", hourNeed) + ":01"))){
            return new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY));
        } else {
            int toDateDays = Math.abs(toDate - localDate.getDayOfWeek().getValue());
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
                .append(DATE_RETURNED).toString();
            LOGGER.info(msgTimeSp);
            return retDate;
        }
    }

    /**
     Дата запуска common scanner
     <p>
     Usage: {@link #toStringS()}

     @param hourNeed час
     @param minNeed  минута
     @return new {@link Date} следующая суббота 0:01
     */
    private static Date getNextSat(int hourNeed, int minNeed) {
        final long stArt = System.currentTimeMillis();
        Calendar.Builder builder = new Calendar.Builder();
        LocalDate localDate = LocalDate.now();
        DayOfWeek satDay = DayOfWeek.SATURDAY;
        if(localDate.getDayOfWeek().toString().equalsIgnoreCase(satDay.toString())){
            timeInfo.computeDetails();
            return new Date(timeInfo.getReturnTime() + TimeUnit.MINUTES.toMillis(14));
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
                .append(DATE_RETURNED).toString();
            LOGGER.info(msgTimeSp);
            return retDate;
        }
    }

    /**
     Usages: {@link #toStringS()}

     @return дата через месяц.
     */
    private static LocalDateTime getNextMonth() {
        LocalDateTime localDateTime = LocalDateTime.now();
        localDateTime = localDateTime.plusMonths(1);
        String msg = new StringBuilder()
            .append(" and ret date is: ")
            .append(localDateTime.toString())
            .append("\n").toString();
        LOGGER.info(msg);
        return localDateTime;
    }
}