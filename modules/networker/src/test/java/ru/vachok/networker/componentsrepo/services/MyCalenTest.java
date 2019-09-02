// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.services;


import org.apache.commons.net.ntp.TimeInfo;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;

import java.text.*;
import java.time.DayOfWeek;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertNull;


/**
 @see MyCalen
 @since 29.07.2019 (0:01) */
public class MyCalenTest {
    
    
    @Test
    public void testGetTimeInfo() {
        TimeInfo info = MyCalen.getTimeInfo();
        info.computeDetails();
        long returnTime = info.getReturnTime();
        Assert.assertTrue((System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(5)) < returnTime, MessageFormat
                .format("{0} (returned) | {1} (true date)", new Date(returnTime).toString(), new Date()));
    }
    
    @Test
    public void testToStringS() {
        String toStr = MyCalen.toStringS();
        Assert.assertTrue(toStr.contains("getNextDayofWeek (FRI) ="), toStr);
    }
    
    @Test
    public void testGetNextDayofWeek() {
        Date nextSun = MyCalen.getNextDayofWeek(10, 10, DayOfWeek.SUNDAY);
        Assert.assertTrue(nextSun.getTime() > System.currentTimeMillis(), nextSun.toString());
        System.out.println("nextSun = " + nextSun);
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