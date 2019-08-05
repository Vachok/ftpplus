// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.fsworks;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractNetworkerFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.util.Collections;


/**
 @see FilesWorkerFactory1
 @since 19.07.2019 (22:49) */
public class FilesWorkerFactoryTest {
    
    
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
    
    @Test
    public void testGetFactoryAbstract() {
        AbstractNetworkerFactory instance = null;
        try {
    
            instance = AbstractNetworkerFactory.getInstance(FilesWorkerFactory1.class.getTypeName());
        }
        catch (IllegalArgumentException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Assert.assertTrue(instance.toString().contains("UpakFiles{"));
        String upakAbstract = ((UpakFiles) instance).packFiles(Collections.singletonList(new File("thr_ThreadConfig-stack.txt")), "test.zip");
        Assert.assertTrue(upakAbstract.contains("test.zip"), upakAbstract);
    }
    
    
}