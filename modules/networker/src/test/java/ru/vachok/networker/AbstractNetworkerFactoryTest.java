// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.abstr.monitors.NetFactory;
import ru.vachok.networker.abstr.monitors.PingerService;
import ru.vachok.networker.componentsrepo.exceptions.IllegalConnectException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.net.enums.SwitchesWiFi;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.concurrent.*;


public class AbstractNetworkerFactoryTest {
    
    
    private static final String MONITOR_PARAMETER = "kudr";
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private InetAddress testAddress;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @BeforeMethod
    public void setTestAddress() {
        byte[] addressBytes = new byte[0];
        try {
            addressBytes = InetAddress.getByName("10.200.213.254").getAddress();
        }
        catch (UnknownHostException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        
        try {
            this.testAddress = InetAddress.getByAddress(addressBytes);
        }
        catch (UnknownHostException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testCreateNetMonitorFactory() {
        PingerService monitorFactory = AbstractNetworkerFactory.getInstance(NetFactory.class.getTypeName());
        boolean isIPReach = false;
    
        try {
            byte[] addressBytes = InetAddress.getByName("10.200.213.254").getAddress();
            isIPReach = monitorFactory.isReach(InetAddress.getByAddress(addressBytes));
        }
        catch (UnknownHostException e) {
            messageToUser.error(MessageFormat.format("AbstractNetworkerFactoryTest.testCreateNetMonitorFactory: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        Assert.assertTrue(isIPReach);
    }
    
    @Test
    public void testCreateSSHFactory() {
        AbstractNetworkerFactory abstractNetworkerFactory = AbstractNetworkerFactory.getInstance(SSHFactory.class.getTypeName());
        Assert.assertTrue(abstractNetworkerFactory.toString().contains("ru.vachok.networker.SSHFactory"));
    }
    
    @Test
    public void testGetInstance() {
        AbstractNetworkerFactory instance = AbstractNetworkerFactory.getInstance();
        Assert.assertTrue(instance.toString().contains("pingSleepMsec=20"));
    }
    
    @Test
    public void getSSHFactoryOverAbsFactory() {
        AbstractNetworkerFactory abstractNetworkerFactory = AbstractNetworkerFactory.getInstance(SSHFactory.class.getTypeName());
        Callable<String> factory = abstractNetworkerFactory
            .getSSHFactory(SwitchesWiFi.HOSTNAME_SRVGITEATMEATRU, "ls", this.getClass().getSimpleName());
        
        try {
            Future<String> stringFuture = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(factory);
            if (abstractNetworkerFactory.isReach(InetAddress.getByName(SwitchesWiFi.HOSTNAME_SRVGITEATMEATRU))) {
                stringFuture.get(ConstantsFor.DELAY / 2, TimeUnit.SECONDS);
            }
            else {
                throw new IllegalConnectException((AbstractNetworkerFactory) factory);
            }
        }
        catch (InterruptedException | ExecutionException | TimeoutException | UnknownHostException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void getPing() {
        AbstractNetworkerFactory abstractNetworkerFactory = AbstractNetworkerFactory.getInstance(NetFactory.class.getTypeName());
        boolean factoryReach = abstractNetworkerFactory.isReach(testAddress);
    }
    
}