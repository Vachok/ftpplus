// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.TestConfigure;
import ru.vachok.networker.net.enums.OtherKnownDevices;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.testng.Assert.assertNull;


/**
 @since 10.06.2019 (9:33) */
public class NameOrIPCheckerTest {
    
    
    private final TestConfigure testConfigure = new TestConfigure(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigure.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigure.afterClass();
    }
    
    @Test
    public void testCheckPat() {
        try {
            InetAddress inetAddress = new NameOrIPChecker(OtherKnownDevices.DO0213_KUDR.replace(ConstantsFor.DOMAIN_EATMEATRU, "")).resolveIP();
            String ipString = inetAddress.toString();
            Assert.assertTrue(ipString.contains(OtherKnownDevices.DO0213_KUDR));
        }
        catch (UnknownHostException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    @Test
    public void testResolveIP() {
        try {
            new NameOrIPChecker("1.1.1.1").resolveIP();
        }
        catch (UnknownHostException e) {
            assertNull(e, e.getMessage());
        }
    }
}