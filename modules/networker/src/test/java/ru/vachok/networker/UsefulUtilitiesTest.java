// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.jetbrains.annotations.NotNull;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.mailserver.MailRule;
import ru.vachok.networker.net.scanner.NetListsTest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


/**
 @see UsefulUtilities
 @since 10.08.2019 (11:40) */
public class UsefulUtilitiesTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testGetMailRules() {
        ConcurrentMap<Integer, MailRule> mailRulesMap = UsefulUtilities.getMailRules();
        Assert.assertNotNull(mailRulesMap);
    }
    
    @Test
    public void testIsPingOK() {
        boolean isOk = UsefulUtilities.isPingOK();
        Assert.assertTrue(isOk);
    }
    
    @Test
    public void testGetStringsVisit() {
        String[] visit = UsefulUtilities.getStringsVisit();
        Assert.assertTrue(Arrays.toString(visit).contains(".own"), Arrays.toString(visit));
    }
    
    @Test
    public void testThisPC() {
        String thisPCStr = UsefulUtilities.thisPC();
        try {
            InetAddress locHost = InetAddress.getLocalHost();
            Assert.assertEquals(thisPCStr, locHost.getHostName());
        }
        catch (UnknownHostException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetVis() {
        Visitor vis = UsefulUtilities.getVis(new MockHttpServletRequest());
        Assert.assertTrue(vis.toString().contains("Visitor{clickCounter"), vis.toString());
    }
    
    @Test
    public void testGetMyTime() {
        long myTime = UsefulUtilities.getMyTime();
        Assert.assertEquals(myTime, 442278120);
    }
    
    @Test
    public void testGetDelay() {
        long delay = UsefulUtilities.getDelay();
        Assert.assertEquals(delay, 17);
    }
    
    @Test
    public void testGetUpTime() {
        String upTimeStr = UsefulUtilities.getUpTime();
        Assert.assertTrue(upTimeStr.contains("h uptim"), upTimeStr);
    }
    
    @Test
    public void testGetAtomicTime() {
        long atomTime = UsefulUtilities.getAtomicTime();
        Assert.assertTrue(atomTime > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10)), String.valueOf(atomTime - System.currentTimeMillis()));
    }
    
    @Test
    public void testGetHTMLCenterRed() {
        String testColor = UsefulUtilities.getHTMLCenterColor("test", "red");
        Assert.assertTrue(testColor.contains("color=\"red\""), testColor);
        testColor = UsefulUtilities.getHTMLCenterColor("test", ConstantsFor.GREEN);
        Assert.assertTrue(testColor.contains("color=\"green\""), testColor);
    }
    
    @Test
    public void testIpFlushDNS() {
        String ipFlushDNS = UsefulUtilities.ipFlushDNS();
        Assert.assertTrue(ipFlushDNS.contains("Windows"), ipFlushDNS);
    }
    
    @Test
    public void testGetDeleteTrashPatterns() {
        @NotNull String[] deleteTrashPatterns = UsefulUtilities.getDeleteTrashPatterns();
        String asString = Arrays.toString(deleteTrashPatterns);
        Assert.assertTrue(asString.contains("DELETE  FROM `inetstats` WHERE `site` LIKE '%clients1.google%'"), asString);
        System.out.println("asString = " + asString);
    }
}