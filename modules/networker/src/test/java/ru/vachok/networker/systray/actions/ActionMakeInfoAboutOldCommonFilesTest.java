// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray.actions;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.componentsrepo.data.enums.FileNames;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;


/**
 @see ActionMakeInfoAboutOldCommonFiles
 @since 30.07.2019 (11:00) */
public class ActionMakeInfoAboutOldCommonFilesTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void testActionPerformed() {
        File oldFile = new File(FileNames.FILENAME_OLDCOMMON + "." + Math.random());
        ActionMakeInfoAboutOldCommonFiles actionMake = new ActionMakeInfoAboutOldCommonFiles();
        actionMake.setTimeoutSeconds(5);
        actionMake.setFileName(oldFile.getName());
        try {
            String makeAction = actionMake.makeAction();
        }
        catch (InvokeIllegalException e) {
            Assert.assertTrue(e.getMessage().contains("TIMEOUT"), e.getMessage());
        }
        oldFile.deleteOnExit();
    }
    
    @Test
    public void testTestToString() {
        Assert.assertTrue(new ActionMakeInfoAboutOldCommonFiles().toString().contains("ActionMakeInfoAboutOldCommonFiles["));
    }
}