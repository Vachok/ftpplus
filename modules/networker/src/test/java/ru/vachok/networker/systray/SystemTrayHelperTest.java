package ru.vachok.networker.systray;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.awt.*;


/**
 @see SystemTrayHelper
 @since 19.08.2019 (11:06) */
public class SystemTrayHelperTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(SystemTrayHelper.class
            .getSimpleName(), System.nanoTime());
    
    private SystemTrayHelper systemTrayHelper;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        this.systemTrayHelper = (SystemTrayHelper) SystemTrayHelper.getI().get();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testGetI() {
        Assert.assertNotNull(systemTrayHelper);
    }
    
    @Test
    public void testTrayAdd() {
        systemTrayHelper.trayAdd();
        TrayIcon trayIcon = systemTrayHelper.getTrayIcon();
        Assert.assertNotNull(trayIcon);
        trayIcon.displayMessage("test", "test", TrayIcon.MessageType.INFO);
        Image iconImage = trayIcon.getImage();
        Assert.assertNotNull(iconImage);
    }
    
    @Test
    public void testToString() {
        String toStr = systemTrayHelper.toString();
        Assert.assertTrue(toStr.contains("SystemTrayHelper{"), toStr);
    }
    
    @Test
    public void testDelOldActions() {
        Assert.assertTrue(systemTrayHelper.getTrayIcon().getActionListeners().length > 0);
        systemTrayHelper.delOldActions();
        Assert.assertFalse(systemTrayHelper.getTrayIcon().getActionListeners().length > 0);
    }
    
    @Test
    public void testGetTrayIcon() {
        Assert.assertNotNull(systemTrayHelper.getTrayIcon());
    }
}