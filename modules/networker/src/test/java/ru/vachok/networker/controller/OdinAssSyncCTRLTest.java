package ru.vachok.networker.controller;


import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.text.MessageFormat;


/**
 @see OdinAssSyncCTRL
 @since 09.08.2019 (13:25) */
public class OdinAssSyncCTRLTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(OdinAssSyncCTRL.class
        .getSimpleName(), System.nanoTime());
    
    private OdinAssSyncCTRL odinAssSyncCTRL = new OdinAssSyncCTRL();
    
    private Model model = new ExtendedModelMap();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testUploadFiles() {
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        MultipartFile file = new MockMultipartFile("test", "test".getBytes());
        String files = odinAssSyncCTRL.uploadFiles(file, redirectAttributes, model);
        Assert.assertEquals(files, "odinass");
        Assert.assertTrue(model.asMap().size() == 4);
        
        Assert.assertTrue(model.asMap().get("CsvTxt").toString().contains("CsvTxt{"));
        Assert.assertTrue(model.asMap().get("mapfiles").toString().contains("a href"));
        
        Assert.assertTrue(model.asMap().get("csvparse").toString().isEmpty());
    }
    
    @Test
    public void testViewPage() {
        String viewPage = odinAssSyncCTRL.viewPage(model);
        Assert.assertEquals(viewPage, "odinass");
        Assert.assertTrue(model.asMap().size() == 6, MessageFormat.format("model.asMap().size({0})", model.asMap().size()));
        Assert.assertEquals(model.asMap().get("title").toString(), "OdinAssSyncCTRL");
    }
    
    @Test
    public void testResetForm() {
        String resetForm = odinAssSyncCTRL.resetForm();
        Assert.assertEquals(resetForm, "redirect:/odinass");
    }
    
    @Test
    public void testTestToString() {
        String toStr = odinAssSyncCTRL.toString();
        Assert.assertTrue(toStr.contains("OdinAssSyncCTRL{"), toStr);
    }
}