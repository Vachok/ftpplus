// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.SwitchesWiFi;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.concurrent.*;


/**
 @see AbstractNetworkerFactory
 @since 16.07.2019 (10:46) */
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
        AbstractNetworkerFactory abstractNetworkerFactory = AbstractNetworkerFactory.netScanServiceFactory();
        boolean isIPReach = false;
        
        try {
            byte[] addressBytes = InetAddress.getByName("10.200.213.254").getAddress();
            isIPReach = abstractNetworkerFactory.isReach(InetAddress.getByAddress(addressBytes));
        }
        catch (UnknownHostException e) {
            messageToUser.error(MessageFormat.format("AbstractNetworkerFactoryTest.testCreateNetMonitorFactory: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        Assert.assertTrue(isIPReach);
    }
    
    @Test
    public void testCreateSSHFactory() {
        Callable<String> sshFactory = AbstractNetworkerFactory.getSSHFactory("192.168.13.42", "ls", this.getClass().getSimpleName());
        Assert.assertTrue(sshFactory.toString().contains("SSHFactory{classCaller='AbstractNetworkerFactoryTest'"), sshFactory.toString());
    }
    
    @Test
    public void testGetInstance() {
        AbstractNetworkerFactory instance = AbstractNetworkerFactory.getInstance();
        Assert.assertTrue(instance.toString().contains("NetPinger{pingResultStr="), instance.toString());
    }
    
    @Test
    public void getSSHFactoryOverAbsFactory() {
        Callable<String> factory = AbstractNetworkerFactory.getSSHFactory(SwitchesWiFi.HOSTNAME_SRVGITEATMEATRU, "ls", this.getClass().getSimpleName());
        
        try {
            Future<String> stringFuture = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(factory);
            if (AbstractNetworkerFactory.getInstance().isReach(InetAddress.getByName(SwitchesWiFi.HOSTNAME_SRVGITEATMEATRU))) {
                String oldGitLS = stringFuture.get(20, TimeUnit.SECONDS);
                Assert.assertNotNull(oldGitLS);
                Assert.assertTrue(oldGitLS.contains("pass"), oldGitLS);
            }
            else {
                throw new InvokeIllegalException("AbstractNetworkerFactoryTest.getSSHFactoryOverAbsFactory invocation is illegal. srv-git.eatmeat.ru is offline");
            }
        }
        catch (InterruptedException | ExecutionException | TimeoutException | UnknownHostException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void getPing() {
        AbstractNetworkerFactory instance = AbstractNetworkerFactory.getInstance();
        boolean factoryReach = instance.isReach(testAddress);
        Assert.assertTrue(factoryReach);
    }
    
}