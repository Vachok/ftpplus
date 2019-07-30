package ru.vachok.networker.net;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;

import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 @see AccessListsCheckUniq
 @since 30.07.2019 (11:21) */
public class AccessListsCheckUniqTest {
    
    
    @Test
    public void testRun() {
        new AccessListsCheckUniq().run();
        File file = new File(ConstantsFor.FILENAME_INETUNIQ);
        Assert.assertTrue(file.exists());
        Assert.assertTrue(file.lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10)));
    }
    
    @Test
    public void testConnectTo() {
        String connectToResult = new AccessListsCheckUniq().connectTo();
        Assert.assertTrue(connectToResult.contains("####### SQUID FULL ##########"), connectToResult);
    }
    
    @Test
    public void testTestToString() {
        String toStr = new AccessListsCheckUniq().toString();
        Assert.assertTrue(toStr.contains("AccessListsCheckUniq["), toStr);
    }
}