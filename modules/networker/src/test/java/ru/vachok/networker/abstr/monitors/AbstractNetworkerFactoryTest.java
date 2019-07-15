// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr.monitors;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractNetworkerFactory;
import ru.vachok.networker.SSHFactory;


/**
 @since 11.07.2019 (16:00) */
public class AbstractNetworkerFactoryTest {
    
    
    private static final String MONITOR_PARAMETER = "kudr";
    
    @Test
    public void getPing() {
        NetNetworkerFactory monitorFactory = AbstractNetworkerFactory.createNetMonitorFactory(MONITOR_PARAMETER);
        Runnable monitoringRunnable = monitorFactory.getMonitoringRunnable();
        Assert.assertTrue(monitoringRunnable.toString().contains("10.200.214.80"), monitoringRunnable.toString());
    }
    
    @Test
    public void getSSHFactoryOverAbsFactory() {
        SSHFactory factory = AbstractNetworkerFactory.createSSHFactory("192.168.13.42", "ls", this.getClass().getSimpleName());
        String call = factory.call();
        Assert.assertTrue(call.contains("id_rsa.pub"), call);
    }
}