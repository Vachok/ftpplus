// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.OtherKnownDevices;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.net.InetAddress;


/**
 @since 10.06.2019 (9:33) */
public class NameOrIPCheckerTest {
    
    
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
    public void testCheckPat() {
        InetAddress inetAddress = new NameOrIPChecker(OtherKnownDevices.DO0213_KUDR.replace(ConstantsFor.DOMAIN_EATMEATRU, "")).resolveIP();
        String ipString = inetAddress.toString();
        Assert.assertTrue(ipString.contains(OtherKnownDevices.DO0213_KUDR));
    }
    
    @Test
    public void testResolveIP() {
        InetAddress inetAddress = new NameOrIPChecker("91.210.86.34").resolveIP();
        System.out.println("inetAddress = " + inetAddress.getHostName());
    }
}