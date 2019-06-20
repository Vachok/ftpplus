package ru.vachok.networker.exe.schedule;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.awt.*;
import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 @since 20.06.2019 (10:09) */
@SuppressWarnings("ALL") public class WeekStatsTest {
    
    
    @Test
    public void testGetPCStats() {
        throw new IllegalComponentStateException("20.06.2019 (10:09)");
    }
    
    @Test
    public void testGetInetStats() {
        throw new IllegalComponentStateException("20.06.2019 (10:09)");
    }
    
    @Test
    public void testRun() {
        WeekStats weekStats = new WeekStats();
        weekStats.run();
        Assert.assertTrue(new File("inetstatsIP.csv").lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1));
    }
}