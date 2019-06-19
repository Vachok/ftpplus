package ru.vachok.networker.net;


import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.awt.*;
import java.io.IOException;
import java.util.List;


/**
 @since 19.06.2019 (16:30) */
public class NetPingerTest {
    
    
    @Test
    public void testPingDev() {
        throw new IllegalComponentStateException("19.06.2019 (16:31)");
    }
    
    @Test
    public void testIsReach() {
        throw new IllegalComponentStateException("19.06.2019 (16:31)");
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