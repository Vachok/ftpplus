// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray.actions;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.FileNames;

import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 @see ActionMakeInfoAboutOldCommonFiles
 @since 30.07.2019 (11:00) */
public class ActionMakeInfoAboutOldCommonFilesTest {
    
    
    @Test
    public void testActionPerformed() {
        File oldFile = new File(FileNames.FILENAME_OLDCOMMON + ".t");
        ActionMakeInfoAboutOldCommonFiles actionMake = new ActionMakeInfoAboutOldCommonFiles();
        try {
            actionMake.makeAction();
        }
        catch (RuntimeException e) {
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