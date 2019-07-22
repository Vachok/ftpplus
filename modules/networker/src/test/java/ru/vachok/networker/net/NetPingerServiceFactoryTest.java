// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractNetworkerFactory;
import ru.vachok.networker.abstr.monitors.NetFactory;
import ru.vachok.networker.abstr.monitors.PingerService;
import ru.vachok.networker.componentsrepo.exceptions.ScanFilesException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.net.enums.OtherKnownDevices;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;


/**
 @since 19.06.2019 (16:30)
 @see NetPingerServiceFactory
 */
public class NetPingerServiceFactoryTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
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
     @see PingerService#pingDevices(java.util.Map)
     */
    @Test
    public void testPingDev() {
        NetFactory abstractNetworkerFactory = (NetFactory) AbstractNetworkerFactory.getInstance(NetFactory.class.getTypeName());
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
    }
    
    @Test
    public void testIsReach() {
        NetPingerServiceFactory netPinger = new NetPingerServiceFactory();
        boolean pingerReach = false;
        try {
            byte[] addressBytes = InetAddress.getByName("10.200.200.1").getAddress();
            pingerReach = netPinger.isReach(InetAddress.getByAddress(addressBytes));
        }
        catch (UnknownHostException e) {
            messageToUser.error(MessageFormat.format("NetPingerServiceTest.testIsReach: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        Assert.assertTrue(pingerReach);
    }
    
    @Test
    public void testRun() {
        NetFactory npFactory = (NetFactory) AbstractNetworkerFactory.getInstance(NetPingerServiceFactory.class.getTypeName());
        try {
            npFactory.run();
        }
        catch (ScanFilesException e) {
            Assert.assertNotNull(e);
        }
        MultipartFile multipartFile = null;
        try {
            multipartFile = new MockMultipartFile("ping2ping.txt", getClass().getResourceAsStream("/static/ping2ping.txt"));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
        ((NetPingerServiceFactory) npFactory).setMultipartFile(multipartFile);
        ((NetPingerServiceFactory) npFactory).setTimeForScanStr("0.1");
        npFactory.run();
        String pingResultStr = npFactory.getPingResultStr();
        System.out.println("pingResultStr = " + pingResultStr);
    }
}