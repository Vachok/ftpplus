// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import org.jetbrains.annotations.NotNull;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.mail.ExSRV;
import ru.vachok.networker.mail.MailRule;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static ru.vachok.networker.componentsrepo.UsefulUtilities.scheduleTrunkPcUserAuto;


/**
 @see UsefulUtilities
 @since 10.08.2019 (11:40) */
public class UsefulUtilitiesTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(UsefulUtilitiesTest.class.getSimpleName(), System.nanoTime());
    
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
        ConcurrentMap<Integer, MailRule> mailRulesMap = ExSRV.getMailRules();
        Assert.assertNotNull(mailRulesMap);
    }
    
    @Test
    public void testIsPingOK() {
        boolean isOk = UsefulUtilities.isPingOK();
        Assert.assertTrue(isOk);
    }
    
    @Test
    public void testGetStringsVisit() {
        List<String> visit = UsefulUtilities.getPatternsToDeleteFilesOnStart();
        String fromArray = new TForms().fromArray(visit);
        Assert.assertTrue(fromArray.contains(".own"), fromArray);
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
    
    @Test(invocationCount = 3)
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
        String testColor = UsefulUtilities.getHTMLCenterColor("red", "test");
        Assert.assertTrue(testColor.contains("color=\"red\""), testColor);
        testColor = UsefulUtilities.getHTMLCenterColor(ConstantsFor.GREEN, "test");
        Assert.assertTrue(testColor.contains("color=\"green\""), testColor);
    }
    
    @Test
    public void testIpFlushDNS() {
        String ipFlushDNS = UsefulUtilities.ipFlushDNS();
        Assert.assertTrue(ipFlushDNS.contains("Windows"), ipFlushDNS);
    }
    
    @Test
    public void testGetDeleteTrashPatterns() {
        @NotNull String[] deleteTrashPatterns = UsefulUtilities.getDeleteTrashInternetLogPatterns();
        String asString = Arrays.toString(deleteTrashPatterns);
        Assert.assertTrue(asString.contains("DELETE  FROM `inetstats` WHERE `site` LIKE '%clients1.google%'"), asString);
        System.out.println("asString = " + asString);
    }
    
    @Test
    public void testGetHTMLCenterColor() {
    }
    
    @Test
    public void testGetIISLogSize() {
    }
    
    @Test
    public void testGetBuildStamp() {
        long buildStamp = UsefulUtilities.getBuildStamp();
        Assert.assertTrue(buildStamp > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)), new Date(buildStamp).toString());
    }
    
    @Test
    public void testGetScansDelay() {
        int scDelay = UsefulUtilities.getScansDelay();
        System.out.println("scDelay = " + scDelay);
        Assert.assertTrue(scDelay < 177);
        Assert.assertTrue(scDelay > 18);
    }
    
    @Test
    public void testGetPatternsToDeleteFilesOnStart() {
        List<String> deleteFilesOnStart = UsefulUtilities.getPatternsToDeleteFilesOnStart();
        String fromArray = new TForms().fromArray(deleteFilesOnStart);
        Assert.assertEquals(fromArray, "\nvisit_\n.tv\n.own\n.rgh");
    }
    
    @Test
    public void testGetRunningInformation() {
        String runningInformation = UsefulUtilities.getRunningInformation();
        Assert.assertTrue(runningInformation.contains("CPU information"), runningInformation);
        Assert.assertTrue(runningInformation.contains("Memory information"), runningInformation);
        Assert.assertTrue(runningInformation.contains("Runtime information"), runningInformation);
    }
    
    @Test
    public void testGetDeleteTrashInternetLogPatterns() {
        @NotNull String[] internetLogPatterns = UsefulUtilities.getDeleteTrashInternetLogPatterns();
        String fromArray = new TForms().fromArray(internetLogPatterns);
        Assert.assertTrue(fromArray.contains("DELETE"));
        Assert.assertTrue(fromArray.contains("LIKE"));
        Assert.assertTrue(fromArray.contains("ceipmsn"));
    }
    
    @Test
    public void testSetPreference() {
        UsefulUtilities.setPreference("test", "test");
        Assert.assertEquals(AppComponents.getUserPref().get("test", ""), "test");
    }
    
    @Test
    public void testGetOS() {
        String os = UsefulUtilities.getOS();
        Assert.assertTrue(os.contains("Windows 10"), os);
    }
    
    @Test
    public void testGetMemory() {
        String memory = UsefulUtilities.getMemory();
        Assert.assertTrue(memory.contains("Heap Memory Usage"), memory);
        Assert.assertTrue(memory.contains("NON Heap Memory Usage"), memory);
        Assert.assertTrue(memory.contains("Object Pending Finalization Count"), memory);
        Assert.assertTrue(memory.contains("Loaded Class Count"), memory);
    }
    
    @Test
    public void testGetRuntime() {
        String runtime = UsefulUtilities.getRuntime();
        Assert.assertTrue(runtime.contains("StartTime"), runtime);
        Assert.assertTrue(runtime.contains("Threading"), runtime);
        Assert.assertTrue(runtime.contains("total threads started"), runtime);
        Assert.assertTrue(runtime.contains("Daemon"), runtime);
    }
    
    @Test
    public void testGetTotalCPUTimeInformation() {
        String totalCPUTime = UsefulUtilities.getTotalCPUTimeInformation();
        Assert.assertTrue(totalCPUTime.contains("Total CPU time for all threads"), totalCPUTime);
    }
    
    @Test(invocationCount = 3)
    public void testScheduleTrunkPcUserAuto() {
        try (ConfigurableApplicationContext context = IntoApplication.getConfigurableApplicationContext()) {
            context.start();
            Assert.assertTrue(context.isRunning());
            String userAuto = scheduleTrunkPcUserAuto();
            String schedToStr = AppComponents.threadConfig().getTaskScheduler().toString();
            Assert.assertEquals(userAuto, schedToStr);
            context.stop();
            Assert.assertFalse(context.isRunning());
        }
        catch (IllegalStateException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetTotCPUTime() {
        String totCPUTime = UsefulUtilities.getTotCPUTime();
        Assert.assertTrue(totCPUTime.contains("sec. (user -"), totCPUTime);
    }
    
    @Test
    public void testGetDelayMs() {
        long delayMs = UsefulUtilities.getDelayMs();
        System.out.println("delayMs = " + TimeUnit.MILLISECONDS.toDays(delayMs));
    }
}