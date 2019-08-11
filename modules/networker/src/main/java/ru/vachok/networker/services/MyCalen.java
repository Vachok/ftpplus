// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.apache.commons.net.ntp.TimeInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.restapi.message.DBMessenger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 Работа с календарём
 
 @since 09.12.2018 (15:26) */
@SuppressWarnings("SameParameterValue")
public abstract class MyCalen {
    
    
    public static final String JAVA_LANG_STRING_NAME = "java.lang.String";
    
    private static final String DATE_RETURNED = " date returned";
    
    /**
     {@link TimeChecker}
     */
    private static final TimeChecker TIME_CHECKER = new TimeChecker();
    
    private static MessageToUser messageToUser = new DBMessenger(MyCalen.class.getSimpleName());
    
    /**
     {@link TimeChecker#call()}
     */
    @SuppressWarnings("CanBeFinal")
    private static TimeInfo timeInfo = TIME_CHECKER.call();
    
    @Contract(pure = true)
    private MyCalen() {
    
    }
    
    /**
     @return {@link TimeInfo}
     */
    @Contract(pure = true)
    public static TimeInfo getTimeInfo() {
        return timeInfo;
    }
    
    /**
     Проверка работоспособности.
     <p>
     {@code getNextDayofWeek(0, 4, DayOfWeek.FRIDAY; getNextMonth(); getNextSat(0, 2); getNextDayofWeek(10, 2, DayOfWeek.MONDAY)}
     
     @return результаты методов.
     */
    public static @NotNull String toStringS() {
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
 
     @param hourNeed час
     @param minNeed минута
     @param dayOfWeek день недели
     @return нужный {@link Date}
     */
    public static @NotNull Date getNextDayofWeek(int hourNeed, int minNeed, @NotNull DayOfWeek dayOfWeek) {
        final long stArt = System.currentTimeMillis();
        Calendar.Builder cBuilder = new Calendar.Builder();
        LocalDate localDate = LocalDate.now();
        int toDate = dayOfWeek.getValue();
        if (dayOfWeek.equals(LocalDate.now().getDayOfWeek())) {
            toDate = dayOfWeek.getValue() + 7;
        }
        if (localDate.getDayOfWeek().equals(dayOfWeek) && LocalTime.now().isBefore(LocalTime.parse(String.format("%02d", hourNeed) + ":01"))) {
            return new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY));
        }
        else {
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
                .append(retDate)
                .append(DATE_RETURNED).toString();
            messageToUser.info(msgTimeSp);
            return retDate;
        }
    }
    
    /**
     Очистка pcuserauto
     */
    private static void trunkTableUsers() {
        try (Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
             PreparedStatement preparedStatement = c.prepareStatement("TRUNCATE TABLE pcuserauto")
        ) {
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    
    public static @NotNull Date getNextDay(int needHour, int needMin) {
        LocalDateTime localDateTime = LocalDateTime.now();
        Calendar.Builder builder = new Calendar.Builder();
        builder.setDate(localDateTime.getYear(), localDateTime.getMonth().getValue() - 1, localDateTime.getDayOfMonth() + 1)
            .setTimeOfDay(needHour, needMin, 0);
        return builder.build().getTime();
    }
    
    public static @NotNull Date getThisDay(int hourNeed, int minuteNeed) {
        Calendar.Builder builder = new Calendar.Builder();
        LocalDateTime nowTime = LocalDateTime.now();
        builder.setDate(nowTime.getYear(), nowTime.getMonth().getValue() - 1, nowTime.getDayOfMonth());
        builder.setTimeOfDay(hourNeed, minuteNeed, 0);
        return builder.build().getTime();
    }
    
    /**
     @param scheduledExecutorService {@link ScheduledExecutorService}
     @return {@code msg = dateFormat.format(dateStart) + " pcuserauto (" + TimeUnit.MILLISECONDS.toHours(delayMs) + " delay hours)}
     */
    public static @NotNull String planTruncateTableUsers(@NotNull ScheduledExecutorService scheduledExecutorService) {
        messageToUser.info(ConstantsFor.STR_INPUT_OUTPUT, "", JAVA_LANG_STRING_NAME);
        
        Date dateStart = getNextDayofWeek(8, 30, DayOfWeek.MONDAY);
        DateFormat dateFormat = new SimpleDateFormat("MM.dd, hh:mm", Locale.getDefault());
        long delayMs = dateStart.getTime() - System.currentTimeMillis();
        String msg = dateFormat.format(dateStart) + " pcuserauto (" + TimeUnit.MILLISECONDS.toHours(delayMs) + " delay hours)";
    
        scheduledExecutorService.scheduleWithFixedDelay(MyCalen::trunkTableUsers, delayMs, ConstantsFor.ONE_WEEK_MILLIS, TimeUnit.MILLISECONDS);
        messageToUser.infoNoTitles("msg = " + msg);
        return msg;
    }
    
    public static long getLongFromDate(int day, int month, int year, int hour, int minute) {
        Calendar.Builder builder = new Calendar.Builder();
        builder.setDate(year, month - 1, day);
        builder.setTimeOfDay(hour, minute, 0);
        return builder.build().getTimeInMillis();
    }
    
    /**
     Дата запуска common scanner
     <p>
     Usage: {@link #toStringS()}
     
     @param hourNeed час
     @param minNeed минута
     @return new {@link Date} следующая суббота 0:01
     */
    private static @NotNull Date getNextSat(int hourNeed, int minNeed) {
        final long stArt = System.currentTimeMillis();
        Calendar.Builder builder = new Calendar.Builder();
        LocalDate localDate = LocalDate.now();
        DayOfWeek satDay = DayOfWeek.SATURDAY;
        if (localDate.getDayOfWeek().toString().equalsIgnoreCase(satDay.toString())) {
            timeInfo.computeDetails();
            return new Date(timeInfo.getReturnTime() + TimeUnit.MINUTES.toMillis(14));
        }
        else {
            int toSat = satDay.getValue() - localDate.getDayOfWeek().getValue();
            Date retDate = builder
                .setDate(
                    localDate.getYear(),
                    localDate.getMonth().getValue() - 1,
                    localDate.getDayOfMonth() + toSat)
                .setTimeOfDay(hourNeed, minNeed, 0).build().getTime();
            timeInfo.computeDetails();
            
            String msgTimeSp = new StringBuilder()
                .append(retDate)
                .append(" ")
                .append(toSat)
                .append("\nTimeChecker information: ")
                .append(timeInfo.getMessage())
                .append("\nMyCalen.getNextSat method. ")
                .append((float) (System.currentTimeMillis() - stArt) / 1000)
                .append(" sec spend\n")
                .append(retDate)
                .append(DATE_RETURNED).toString();
            messageToUser.info(msgTimeSp);
            return retDate;
        }
    }
    
    /**
     Usages: {@link #toStringS()}
     
     @return дата через месяц.
     */
    private static @NotNull LocalDateTime getNextMonth() {
        LocalDateTime localDateTime = LocalDateTime.now();
        localDateTime = localDateTime.plusMonths(1);
        String msg = new StringBuilder()
            .append(" and ret date is: ")
            .append(localDateTime)
            .append("\n").toString();
        messageToUser.info(msg);
        return localDateTime;
    }
}