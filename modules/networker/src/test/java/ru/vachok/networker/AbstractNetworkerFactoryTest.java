// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.abstr.monitors.NetFactory;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;


public class AbstractNetworkerFactoryTest {
    
    
    @Test
    public void testCreateNetMonitorFactory() {
        NetFactory monitorFactory = AbstractNetworkerFactory.createNetMonitorFactory();
        boolean isIPReach = monitorFactory.isReach("10.200.213.254");
        Assert.assertTrue(isIPReach);
    }
    
    @Test
    public void testCreateSSHFactory() {
        throw new InvokeEmptyMethodException("17.07.2019 (11:27)");
    }
    
    @Test
    public void testGetInstance() {
        throw new InvokeEmptyMethodException("17.07.2019 (11:27)");
    }
    
    @Test
    public void testTestMethod() {
        throw new InvokeEmptyMethodException("17.07.2019 (11:27)");
    }
}