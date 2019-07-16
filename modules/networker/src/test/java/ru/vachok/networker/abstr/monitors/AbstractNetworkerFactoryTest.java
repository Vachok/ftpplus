// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr.monitors;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractNetworkerFactory;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;


/**
 @since 11.07.2019 (16:00) */
public class AbstractNetworkerFactoryTest {
    
    
    private static final String MONITOR_PARAMETER = "kudr";
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
    }
    
    @Test
    public void getPing() {
        NetNetworkerFactory monitorFactory = AbstractNetworkerFactory.createNetMonitorFactory(MONITOR_PARAMETER);
        Runnable monitoringRunnable = monitorFactory.getMonitoringRunnable();
        Assert.assertTrue(monitoringRunnable.toString().contains("10.200.214.80"), monitoringRunnable.toString());
    }
    
    @Test
    public void getSSHFactoryOverAbsFactory() {
        SSHFactory factory = AbstractNetworkerFactory.createSSHFactory("192.168.13.42", "ls", this.getClass().getSimpleName());
        try {
            String call = factory.call();
            Assert.assertTrue(call.contains("id_rsa.pub"), call);
        }
        catch (Exception e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".getSSHFactoryOverAbsFactory", e));
        }
    }
}