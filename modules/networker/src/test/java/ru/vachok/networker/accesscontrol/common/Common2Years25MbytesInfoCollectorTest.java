package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.io.IOException;


/**
 @since 17.06.2019 (14:41) */
public class Common2Years25MbytesInfoCollectorTest {
    
    
    @Test
    public void testCall() {
        File resultFileCSV = new File(getClass().getSimpleName() + ".csv");
        Common2Years25MbytesInfoCollector common2Years25MbytesInfoCollector = new Common2Years25MbytesInfoCollector(resultFileCSV.getAbsolutePath(), true);
        String startPath = common2Years25MbytesInfoCollector.getStartPath();
        Assert.assertEquals(startPath, "\\\\srv-fs.eatmeat.ru\\common_new\\14_ИТ_служба\\Общая");
        String callY2K = null;
        try {
            callY2K = common2Years25MbytesInfoCollector.call();
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
    
        if (resultFileCSV.exists()) {
            Assert.assertTrue(callY2K.contains("Common2Years25MbytesInfoCollectorTest.csv"), callY2K);
            FileSystemWorker.readFile(resultFileCSV.getAbsolutePath());
        }
        else {
            String logAbsolutePathString = FileSystemWorker.writeFile(getClass().getSimpleName() + ".log", callY2K);
            Assert.assertTrue(new File(logAbsolutePathString).exists());
        }
    }
}