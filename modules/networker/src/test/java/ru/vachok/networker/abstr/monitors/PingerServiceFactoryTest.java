// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr.monitors;


import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.net.NetPingerServiceFactory;


/**
 @see PingerServiceFactory
 @since 18.07.2019 (15:32) */
public class PingerServiceFactoryTest {
    
    
    @Test
    public void pingSomethingOverService() {
        PingerService pingerService = new NetPingerServiceFactory();
        String pingResultStr = pingerService.getPingResultStr();
        System.out.println("pingResultStr = " + pingResultStr);
    }
    
    @Test
    public void testGetExecution() {
        throw new InvokeEmptyMethodException("18.07.2019 (15:45)");
    }
    
    @Test
    public void testGetPingResultStr() {
        throw new InvokeEmptyMethodException("18.07.2019 (15:45)");
    }
    
    @Test
    public void testIsReach() {
        throw new InvokeEmptyMethodException("18.07.2019 (15:45)");
    }
    
    @Test
    public void testWriteLogToFile() {
        throw new InvokeEmptyMethodException("18.07.2019 (15:45)");
    }
}