// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.awt.*;
import java.util.concurrent.Executors;


/**
 @see ActionDelTMP
 @since 30.07.2019 (10:57) */
public class ActionDelTMPTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void testActionPerformed() {
        ActionDelTMP actionDelTMP = new ActionDelTMP(Executors.newSingleThreadExecutor(), new MenuItem(), new PopupMenu());
        actionDelTMP.actionPerformed(null);
        throw new TODOException("01.08.2019 (20:18)");
    }
    
    @Test
    public void testTestToString() {
        String toStr = new ActionDelTMP(Executors.newSingleThreadExecutor(), new MenuItem(), new PopupMenu()).toString();
        Assert.assertTrue(toStr.contains("ActionDelTMP{executor=java.util.concurrent.Executors"), toStr);
    }
}