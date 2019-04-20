package ru.vachok.networker.net;


import java.awt.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;


/**
 @since 19.04.2019 (9:01) */
public class VlansCounter implements Callable<Integer> {
    
    
    @Override public Integer call() throws Exception {
        ConcurrentMap<String, String> onLinesResolve = NetListKeeper.getI().getOnLinesResolve();
        int vlansNum = 0;
        long count = onLinesResolve.keySet().stream().distinct().count();
        throw new IllegalComponentStateException("19.04.2019 (10:46)");
    }
}
