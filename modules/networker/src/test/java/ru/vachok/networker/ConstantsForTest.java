package ru.vachok.networker;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.util.Arrays;


/**
 @see ConstantsFor
 @since 19.08.2019 (15:27) */
public class ConstantsForTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(ConstantsFor.class.getSimpleName(), System
            .nanoTime());
    
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
    public void testGetExcludedFoldersForCleaner() {
        @NotNull String[] forCleaner = ConstantsFor.getExcludedFoldersForCleaner();
        String arrStr = Arrays.toString(forCleaner);
        Assert.assertTrue(arrStr.contains("V02.Инструкции"), arrStr);
    }
}