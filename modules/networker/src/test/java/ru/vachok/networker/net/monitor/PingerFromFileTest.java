// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.monitor;


import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


/**
 @see PingerFromFile
 @since 19.06.2019 (16:30) */
public class PingerFromFileTest {


    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());

    private final MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }

    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }

    /**
     @see NetScanService#pingDevices(java.util.Map)
     */
    @Test
    public void testPingDev() {
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
        Assert.assertFalse(testMap.isEmpty());
    }

    @Test
    public void testRun() {
        NetScanService npFactory = new PingerFromFile();
        try {
            npFactory.run();
        }
        catch (InvokeIllegalException e) {
            Assert.assertNotNull(e);
        }
        MultipartFile multipartFile = null;
        try {
            multipartFile = new MockMultipartFile("ping2ping.txt", getClass().getResourceAsStream("/ping2ping.txt"));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
        ((PingerFromFile) npFactory).setMultipartFile(multipartFile);
        ((PingerFromFile) npFactory).setTimeForScanStr("0.1");
        npFactory.run();
        String pingResultStr = npFactory.getPingResultStr();
        System.out.println("pingResultStr = " + pingResultStr);
    }
}