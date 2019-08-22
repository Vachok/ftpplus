package ru.vachok.networker.controller;


import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.common.OldBigFilesInfoCollector;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.ModelAttributeNames;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.*;


/**
 @see FileCleanerCTRL
 @since 02.08.2019 (16:56) */
public class FileCleanerCTRLTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private FileCleanerCTRL fileCleanerCTRL = new FileCleanerCTRL(new OldBigFilesInfoCollector());
    
    private Model model = new ExtendedModelMap();
    
    private HttpServletResponse response = new MockHttpServletResponse();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void testGetFilesInfo() {
        String ctrlFilesInfo = fileCleanerCTRL.getFilesInfo(model, response);
        Assert.assertEquals(ctrlFilesInfo, "cleaner");
        Assert.assertTrue(model.asMap().size() == 2);
        Assert.assertEquals(model.asMap().get("title"), "Инфо о файлах");
        Assert.assertEquals(model.asMap().get(ModelAttributeNames.ATT_BIGOLDFILES)
            .toString(), "Common2Years25MbytesInfoCollector{, fileName='files_2.5_years_old_25mb.csv', date='null', startPath='\\\\srv-fs.eatmeat.ru\\common_new', dirsCounter=0, filesCounter=0, filesSize=0, filesMatched=0, msgBuilder=}");
    }
    
    @Test
    public void testPostFile() {
        Future<String> future = Executors.newSingleThreadExecutor().submit(()->fileCleanerCTRL.postFile(model, new OldBigFilesInfoCollector()));
        try {
            future.get(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException | TimeoutException | ExecutionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    public void testTestToString() {
        Assert.assertTrue(fileCleanerCTRL.toString().contains("FileCleanerCTRL{oldBigFilesInfoCollector=Common2Years25MbytesInfoCollector{"), fileCleanerCTRL
            .toString());
    }
}