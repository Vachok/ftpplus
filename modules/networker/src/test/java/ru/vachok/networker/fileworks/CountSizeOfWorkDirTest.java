// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import static org.testng.Assert.assertNull;


/**
 @see CountSizeOfWorkDir
 @since 24.06.2019 (22:07) */
public class CountSizeOfWorkDirTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
    }
    
    
    /**
     @see CountSizeOfWorkDir#call()
     */
    @Test
    public void testCall() {
        CountSizeOfWorkDir countSizeOfWorkDir = new CountSizeOfWorkDir();
        try {
            System.out.println("countSizeOfWorkDir.call() = " + countSizeOfWorkDir.call());
        }
        catch (Exception e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
}