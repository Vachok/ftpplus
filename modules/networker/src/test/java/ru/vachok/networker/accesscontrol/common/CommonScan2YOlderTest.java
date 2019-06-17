package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;


/**
 @since 17.06.2019 (14:41) */
public class CommonScan2YOlderTest {
    
    
    @Test
    public void testCall() {
        CommonScan2YOlder commonScan2YOlder = new CommonScan2YOlder("file.name");
        String callY2K = commonScan2YOlder.call();
        if (!(new File("files_2.5_years_old_25mb.csv").exists())) {
            Assert.assertFalse(callY2K.isEmpty(), callY2K);
        }
        else {
            Assert.assertEquals(callY2K, "files_2.5_years_old_25mb.csv (Не удается найти указанный файл)");
        }
    }
}