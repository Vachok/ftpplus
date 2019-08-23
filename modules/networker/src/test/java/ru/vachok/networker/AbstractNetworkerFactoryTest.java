// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.SwitchesWiFi;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.net.monitor.PingerFromFile;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.*;


/**
 @see AbstractNetworkerFactory
 @since 16.07.2019 (10:46) */
public class AbstractNetworkerFactoryTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
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
    public void testCreateNetMonitorFactory() {
        PingerFromFile netScanServiceFactory = AbstractNetworkerFactory.netScanServiceFactory();
        boolean isIPReach = NetScanService.isReach("10.200.213.254");
        Assert.assertTrue(isIPReach);
    }
    
    @Test
    public void testCreateSSHFactory() {
        Callable<String> sshFactory = AbstractNetworkerFactory.getSSHFactory("192.168.13.42", "ls", this.getClass().getSimpleName());
        Assert.assertTrue(sshFactory.toString().contains("SSHFactory{classCaller='AbstractNetworkerFactoryTest'"), sshFactory.toString());
    }
    
    @Test
    public void getSSHFactoryOverAbsFactory() {
        SSHFactory factory = AbstractNetworkerFactory.getSSHFactory("192.168.13.42", "ls", this.getClass().getSimpleName());
        
        try {
            Future<String> stringFuture = Executors.newSingleThreadExecutor().submit(factory);
            if (NetScanService.isReach(InetAddress.getByName(SwitchesWiFi.HOSTNAME_SRVGITEATMEATRU).getHostAddress())) {
                String oldGitLS = stringFuture.get(30, TimeUnit.SECONDS);
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
    public void testNetScanServiceFactory() {
        PingerFromFile netScanServiceFactory = AbstractNetworkerFactory.netScanServiceFactory();
        String toStr = netScanServiceFactory.toString();
        Assert.assertTrue(toStr.contains("NetPinger{"), toStr);
    }
    
    @Test
    public void testGetSSHFactory() {
        SSHFactory sshFactory = AbstractNetworkerFactory.getSSHFactory("192.168.13.42", "ls", this.getClass().getSimpleName());
        Future<String> stringFuture = Executors.newSingleThreadExecutor().submit(sshFactory);
        try {
            Assert.assertTrue(stringFuture.get(8, TimeUnit.SECONDS).contains("passwords"));
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
}