package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;


/**
 @see OwnerFixer
 @since 29.07.2019 (13:24) */
public class OwnerFixerTest {
    
    
    private OwnerFixer ownerFixer = new OwnerFixer(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\testClean\\"));
    
    @Test
    public void testRun() {
        ownerFixer.run();
        File fileOwnerFixLog = new File(OwnerFixer.class.getSimpleName() + ".res");
        Assert.assertTrue(fileOwnerFixLog.exists());
        Assert.assertTrue(fileOwnerFixLog.lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10)));
    }
    
    @Test
    public void testTestToString() {
        Assert.assertEquals(ownerFixer.toString(), "OwnerFixer{, resultsList=0, startPath=\\\\srv-fs\\it$$\\ХЛАМ\\testClean}");
    }
}