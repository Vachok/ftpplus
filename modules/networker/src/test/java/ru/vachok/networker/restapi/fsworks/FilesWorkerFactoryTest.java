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


/**
 @see FilesWorkerFactory
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
        try {
            AbstractNetworkerFactory.getInstance("sa");
        }
        catch (IllegalArgumentException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        AbstractNetworkerFactory.setConcreteFactoryName(FilesWorkerFactory.class.getTypeName());
        AbstractNetworkerFactory instance = AbstractNetworkerFactory.getInstance(FilesWorkerFactory.class.getTypeName());
        String statistics = instance.getStatistics();
        System.out.println("statistics = " + statistics);
    }
    
    
}