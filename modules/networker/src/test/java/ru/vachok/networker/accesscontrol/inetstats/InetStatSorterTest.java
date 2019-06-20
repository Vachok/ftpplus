// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;


/**
 @see InetStatSorter */
@SuppressWarnings("ALL") public class InetStatSorterTest {
    
    
    /**
     @see InetStatSorter#run()
     */
    @Test
    public void testRun() {
        InetStatSorter inetStatSorter = new InetStatSorter();
        if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            File noCsv = new File("no.csv");
            inetStatSorter.run();
            Assert.assertFalse(noCsv.exists());
            noCsv.deleteOnExit();
        }
        else {
            Assert.assertTrue(inetStatSorter.toString().contains(LocalDate.now().getDayOfWeek().toString()), inetStatSorter.toString());
        }
    }
}