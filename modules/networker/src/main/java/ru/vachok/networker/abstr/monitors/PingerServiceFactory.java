package ru.vachok.networker.abstr.monitors;


import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;

import java.awt.*;
import java.net.InetAddress;


/**
 @see ru.vachok.networker.abstr.monitors.PingerServiceFactoryTest
 @since 11.07.2019 (17:21) */
public abstract class PingerServiceFactory implements PingerService {
    
    
    @Override
    public String getExecution() {
        throw new InvokeEmptyMethodException("18.07.2019 (15:36)");
    }
    
    @Override
    public String getPingResultStr() {
        throw new InvokeEmptyMethodException("18.07.2019 (15:36)");
    }
    
    @Override
    public boolean isReach(InetAddress inetAddrStr) {
        throw new InvokeEmptyMethodException("18.07.2019 (15:37)");
    }
    
    @Override
    public String writeLogToFile() {
        throw new IllegalComponentStateException("18.07.2019 (15:37)");
    }
}
