// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;


@SuppressWarnings("ALL") public class InetStatSorterTest {
    
    
    @Test
    public void testRun() {
        InetStatSorter inetStatSorter = new InetStatSorter();
        File noCsv = new File("no.csv");
        inetStatSorter.run();
        Assert.assertFalse(noCsv.exists());
        noCsv.deleteOnExit();
    }
}