package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;


/**
 @since 17.06.2019 (14:41) */
public class Common2Years25MbytesInfoCollectorTest {
    
    
    @Test
    public void testCall() {
        Common2Years25MbytesInfoCollector common2Years25MbytesInfoCollector = new Common2Years25MbytesInfoCollector(getClass().getSimpleName() + ".csv", true);
        String callY2K = null;
        try {
            callY2K = common2Years25MbytesInfoCollector.call();
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
        if (new File(getClass().getSimpleName() + ".csv").exists()) {
            Assert.assertTrue(callY2K.isEmpty(), callY2K);
        }
        else {
            Assert.assertEquals(callY2K, getClass().getSimpleName() + ".csv (Не удается найти указанный файл)");
        }
    }
}