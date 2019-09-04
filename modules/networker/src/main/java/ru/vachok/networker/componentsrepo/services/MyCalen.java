// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.services;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 @see MyCalenTest
 @since 09.12.2018 (15:26) */
public abstract class MyCalen {
    
    
    private static final String DATE_RETURNED = " date returned";
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.DB, MyCalen.class.getSimpleName());
    
    @Contract(pure = true)
    private MyCalen() {
    
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
        sb.append(",  IS = ").append(getNextDayofWeek(10, 2, DayOfWeek.MONDAY)).append("\n");
        return sb.toString();
    }
    
    public static @NotNull Date getNextDayofWeek(int hourNeed, int minNeed, @NotNull DayOfWeek dayOfWeek) {
        final long stArt = System.currentTimeMillis();
        Calendar.Builder cBuilder = new Calendar.Builder();
        LocalDate localDate = LocalDate.now();
        int toDate = dayOfWeek.getValue();
        int daysDifference = dayOfWeek.compareTo(LocalDate.now().getDayOfWeek());
        if (daysDifference <= 0) {
            toDate = dayOfWeek.getValue() + 7;
        }
        boolean isToday = localDate.getDayOfWeek().equals(dayOfWeek) && LocalTime.now()
                .isBefore(LocalTime.parse(String.format("%02d", hourNeed) + String.format(":%02d", minNeed)));
        if (isToday) {
            return new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY));
        }
        else {
            int toDateDays = Math.abs(toDate - localDate.getDayOfWeek().getValue());
            cBuilder
                    .setDate(localDate.getYear(),
                            localDate.getMonthValue() - 1,
                            localDate.getDayOfMonth() + toDateDays).setTimeOfDay(hourNeed, minNeed, 0);
        
            Date retDate = cBuilder.build().getTime();
            String msgTimeSp = new StringBuilder()
                    .append("MyCalen.getNextDayofWeek method. ")
                    .append((float) (System.currentTimeMillis() - stArt) / 1000)
                    .append(" sec spend\n")
                    .append(retDate)
                    .append(DATE_RETURNED).toString();
            messageToUser.info(msgTimeSp);
            return retDate;
        }
    }
    
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
    
    public static long getLongFromDate(int day, int month, int year, int hour, int minute) {
        Calendar.Builder builder = new Calendar.Builder();
        builder.setDate(year, month - 1, day);
        builder.setTimeOfDay(hour, minute, 0);
        return builder.build().getTimeInMillis();
    }
    
}