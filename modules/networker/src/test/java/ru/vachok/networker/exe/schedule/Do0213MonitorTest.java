// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.Pinger;
import ru.vachok.networker.componentsrepo.InvokeEmptyMethodException;
import ru.vachok.networker.net.enums.OtherKnownDevices;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertNull;


/**
 @see Do0213Monitor
 @since 07.07.2019 (9:08) */
public class Do0213MonitorTest implements Pinger {
    
    
    @Test
    public void testRun() {
        try {
            new Do0213Monitor().run();
        }
        catch (InvokeEmptyMethodException e) {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void logicRun() {
        try {
            InetAddress inetAddress = InetAddress.getByName(OtherKnownDevices.DO0213_KUDR);
            if (ConstantsFor.thisPC().toLowerCase().contains("home")) {
                Assert.assertFalse(pingDevice(inetAddress));
            }
            else {
                Assert.assertTrue(pingDevice(inetAddress));
            }
        }
        catch (UnknownHostException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        System.out.println(getTimeToEndStr());
    }
    
    @Override public String getTimeToEndStr() {
        return TimeUnit.SECONDS.toMinutes(LocalTime.parse("17:30").toSecondOfDay() - LocalTime.now().toSecondOfDay()) + " minutes left official";
    }
    
    @Override public String getPingResultStr() {
        throw new InvokeEmptyMethodException(getClass().getTypeName());
    }
    
    @Override public boolean isReach(String inetAddrStr) {
        throw new InvokeEmptyMethodException(getClass().getTypeName());
    }
}