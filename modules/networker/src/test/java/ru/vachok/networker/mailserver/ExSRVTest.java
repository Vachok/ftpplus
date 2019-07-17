// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.mailserver;


import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.FileInputStream;
import java.io.IOException;


/**
 @see ExSRV
 @since 22.06.2019 (16:51) */
@SuppressWarnings("ALL") public class ExSRVTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    
    /**
     @see ExSRV#fileAsStrings()
     */
    @Test()
    public void testFileAsStrings() {
        ExSRV exSRV = new ExSRV();
        try {
            String exchSRVFileAsStrings = exSRV.fileAsStrings();
        }
        catch (NullPointerException e) {
            Assert.assertNull(e, e.getMessage());
        }
//        tryWithFile(exSRV);
    }
    
    private void tryWithFile(ExSRV exSRV) {
        String exchSRVFileAsStrings = "null";
        try {
            MultipartFile multipartFile = new MockMultipartFile("rules.txt", new FileInputStream("rules.txt"));
            exSRV.setFile(multipartFile);
            exchSRVFileAsStrings = exSRV.fileAsStrings();
            Assert.assertNotNull(exchSRVFileAsStrings);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        System.out.println(exchSRVFileAsStrings);
        
    }
}