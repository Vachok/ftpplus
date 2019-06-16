// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Deque;


/**
 @since 15.06.2019 (21:11) */
public class NetScanFileWorkerTest {
    
    @Test
    public void testGetDequeOfOnlineDev() {
        try {
            Deque<String> devOnline = NetScanFileWorker.getDequeOfOnlineDev();
        }
        catch (Exception e) {
            Assert.assertNull(e);
        }
    }
}