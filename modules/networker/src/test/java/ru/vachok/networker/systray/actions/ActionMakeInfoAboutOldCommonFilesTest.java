// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray.actions;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;

import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 @see ActionMakeInfoAboutOldCommonFiles
 @since 30.07.2019 (11:00) */
public class ActionMakeInfoAboutOldCommonFilesTest {
    
    
    @Test
    public void testActionPerformed() {
        File oldFile = new File(ConstantsFor.FILENAME_OLDCOMMON + ".t");
        new ActionMakeInfoAboutOldCommonFiles().makeAction(8, oldFile.getName());
        Assert.assertTrue(oldFile.exists());
        Assert.assertTrue(oldFile.lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(15)));
    }
    
    @Test
    public void testTestToString() {
        Assert.assertTrue(new ActionMakeInfoAboutOldCommonFiles().toString().contains("ActionMakeInfoAboutOldCommonFiles["));
    }
}