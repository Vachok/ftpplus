package ru.vachok.networker.net;


import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.abstr.Pinger;
import ru.vachok.networker.net.enums.OtherKnownDevices;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 @since 19.06.2019 (16:30) */
public class NetPingerTest {
    
    
    /**
     @see Pinger#pingDev(java.util.Map)
     */
    @Test
    public void testPingDev() {
        NetPinger netPinger = new NetPinger();
        Map<InetAddress, String> testMap = new HashMap<>();
        for (Field field : OtherKnownDevices.class.getFields()) {
            String fieldName = field.getName();
            try {
                InetAddress inetAddress = InetAddress.getLoopbackAddress();
                String fieldVal = field.get(field).toString();
                if (fieldName.contains("IP")) {
                    inetAddress = InetAddress.getByAddress(InetAddress.getByName(fieldVal).getAddress());
                }
                else {
                    inetAddress = InetAddress.getByName(fieldVal);
                }
                testMap.put(inetAddress, fieldName);
            }
            catch (IllegalAccessException | UnknownHostException ignore) {
                //
            }
        }
        List<String> pingDevList = netPinger.pingDev(testMap);
        Assert.assertTrue(pingDevList.size() == 17);
    }
    
    @Test
    public void testIsReach() {
        NetPinger netPinger = new NetPinger();
        boolean pingerReach = netPinger.isReach("10.200.200.1");
        Assert.assertTrue(pingerReach);
    }
    
    @Test
    public void testRun() {
        NetPinger netPinger = new NetPinger();
        try {
            netPinger.run();
        }
        catch (IllegalComponentStateException e) {
            Assert.assertNotNull(e);
        }
        MultipartFile multipartFile = null;
        try {
            multipartFile = new MockMultipartFile("ping2ping.txt", getClass().getResourceAsStream("ping2ping.txt"));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
        netPinger.setMultipartFile(multipartFile);
        netPinger.setTimeForScanStr("0.1");
        netPinger.run();
        List<String> resList = netPinger.getResList();
        Assert.assertTrue(resList.size() > 2);
    }
}