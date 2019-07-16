package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.abstr.monitors.NetFactory;


public class AbstractNetworkerFactoryTest {
    
    
    @Test
    public void testCreateNetMonitorFactory() {
        NetFactory monitorFactory = AbstractNetworkerFactory.createNetMonitorFactory();
        boolean isIPReach = monitorFactory.isReach("10.200.213.85");
        Assert.assertTrue(isIPReach);
    }
    
    @Test
    public void testCreateSSHFactory() {
    }
    
    @Test
    public void testGetInstance() {
    }
    
    @Test
    public void testTestMethod() {
    }
}