package ru.vachok.ostpst.utils;


import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;


/**
 @since 09.07.2019 (16:43) */
public class OstToPstException extends IllegalStateException {
    
    
    public OstToPstException() {
        System.err.println(getMessage());
    }
    
    @Override
    public String getMessage() {
        StringBuilder stringBuilder = new StringBuilder();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        
        stringBuilder.append(runtimeMXBean.getObjectName());
        stringBuilder.append(new TFormsOST().fromArray(runtimeMXBean.getInputArguments()));
        
        return stringBuilder.toString();
    }
}
