package ru.vachok.networker.services;


import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.services.CsvTxt;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentMap;


/**
 @see CsvTxt
 @since 13.08.2019 (9:36) */
public class CsvTxtTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(CsvTxt.class.getSimpleName(), System
        .nanoTime());
    
    private CsvTxt csvTxt = new CsvTxt();
    
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
    public void testGetPsCommandsList() {
        List<String> commandsList = csvTxt.getPsCommandsList();
        String fromArray = new TForms().fromArray(commandsList);
        Assert.assertTrue(fromArray.isEmpty());
    }
    
    @Test
    public void testSetFile() {
        csvTxt.setFile(new MockMultipartFile("test", "test".getBytes()));
        testGetFile();
        testReadFileToString();
    }
    
    @Test
    public void testGetFiles() {
        ConcurrentMap<String, File> csvTxtFiles = csvTxt.getFiles();
        Assert.assertTrue(csvTxtFiles.isEmpty());
    }
    
    @Test
    public void testGetXlsList() {
        List<String> txtXlsList = csvTxt.getXlsList();
        Assert.assertTrue(txtXlsList.isEmpty());
    }
    
    @Test
    public void testParseXlsx() {
        String parseXlsx = csvTxt.parseXlsx();
        Assert.assertEquals(parseXlsx, "null<br><p>");
    }
    
    @Test
    public void testGetTxtList() {
        List<String> txtTxtList = csvTxt.getTxtList();
        Assert.assertTrue(txtTxtList.isEmpty());
    }
    
    @Test
    public void testTestToString() {
        String toStr = csvTxt.toString();
        Assert.assertTrue(toStr.contains("CsvTxt{"));
    }
    
    private void testGetFile() {
        MultipartFile csvTxtFile = csvTxt.getFile();
        long fileSize = csvTxtFile.getSize();
        Assert.assertEquals(fileSize, 4);
    }
    
    private void testReadFileToString() {
        String fileToString = csvTxt.readFileToString();
        Assert.assertEquals(fileToString, "File is not TXT, XLS or CSV");
    }
}