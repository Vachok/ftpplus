package ru.vachok.networker.systray;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;

import java.awt.*;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 @see ActionDelTMP
 @since 30.07.2019 (10:57) */
public class ActionDelTMPTest {
    
    
    @Test
    public void testActionPerformed() {
        ActionDelTMP actionDelTMP = new ActionDelTMP(Executors.newSingleThreadExecutor(), new MenuItem(), new PopupMenu());
        actionDelTMP.actionPerformed(null);
        Assert.assertTrue(new File(ConstantsFor.FILENAME_CLEANERLOGTXT).exists());
        Assert.assertTrue(new File(ConstantsFor.FILENAME_CLEANERLOGTXT).lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10)));
    }
    
    @Test
    public void testTestToString() {
        String toStr = new ActionDelTMP(Executors.newSingleThreadExecutor(), new MenuItem(), new PopupMenu()).toString();
        Assert.assertTrue(toStr.contains("ActionDelTMP{executor=java.util.concurrent.Executors"), toStr);
    }
}