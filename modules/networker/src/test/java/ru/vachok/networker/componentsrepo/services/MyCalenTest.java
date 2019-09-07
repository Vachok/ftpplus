// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.services;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Date;

import static org.testng.Assert.assertNull;


/**
 @see MyCalen
 @since 29.07.2019 (0:01) */
public class MyCalenTest {
    
    
    @Test
    public void testToStringS() {
        String toStr = MyCalen.toStringS();
        Assert.assertTrue(toStr.contains("getNextDayofWeek (FRI) ="), toStr);
    }
    
    @Test
    public void testGetNextDayofWeek() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E");
        Date nextMon = MyCalen.getNextDayofWeek(9, 10, DayOfWeek.MONDAY);
        Assert.assertTrue(nextMon.getTime() > System.currentTimeMillis(), nextMon.toString());
        Assert.assertTrue(simpleDateFormat.format(nextMon).equalsIgnoreCase("пн"));
        Date afterOneWeek = MyCalen.getNextDayofWeek(9, 10, LocalDate.now().getDayOfWeek());
        
    }
    
    @Test
    public void testGetNextDay() {
        Date nextDay = MyCalen.getNextDay(1, 1);
        Assert.assertTrue(nextDay.getTime() > System.currentTimeMillis());
        System.out.println("nextDay = " + nextDay);
    }
    
    @Test
    public void testGetThisDay() {
        Date thisDay = MyCalen.getThisDay(8, 30);
        Date parsedDate = new Date();
        try {
            parsedDate = new SimpleDateFormat("hh:mm").parse("08:30");
        }
        catch (ParseException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Assert.assertTrue(thisDay.getTime() > parsedDate.getTime(), thisDay.toString());
        System.out.println("thisDay = " + thisDay);
    }
}