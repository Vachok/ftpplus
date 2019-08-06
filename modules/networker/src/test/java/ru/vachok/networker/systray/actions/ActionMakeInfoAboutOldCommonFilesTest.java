// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray.actions;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.enums.FileNames;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 @see ActionMakeInfoAboutOldCommonFiles
 @since 30.07.2019 (11:00) */
public class ActionMakeInfoAboutOldCommonFilesTest {
    
    
    @Test
    public void testActionPerformed() {
        File oldFile = new File(FileNames.FILENAME_OLDCOMMON + ".t");
        ActionMakeInfoAboutOldCommonFiles actionMake = new ActionMakeInfoAboutOldCommonFiles();
        actionMake.setTimeoutSeconds(5);
        try {
            Assert.assertNull(actionMake.makeAction().get(5, TimeUnit.SECONDS));
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Assert.assertTrue(oldFile.exists());
        Assert.assertTrue(oldFile.lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(15)));
    }
    
    @Test
    public void testTestToString() {
        Assert.assertTrue(new ActionMakeInfoAboutOldCommonFiles().toString().contains("ActionMakeInfoAboutOldCommonFiles["));
    }
}