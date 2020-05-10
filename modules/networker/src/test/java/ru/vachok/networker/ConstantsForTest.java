package ru.vachok.networker;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;

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

    @Test
    public void testTestGetExcludedFoldersForCleaner() {
        @NotNull String[] foldersForCleaner = ConstantsFor.getExcludedFoldersForCleaner();
        System.out.println("foldersForCleaner = " + AbstractForms.fromArray(foldersForCleaner));
    }

    @Test
    public void testNoRunOn() {
        if (!ConstantsFor.argNORUNExist("home")) {
            System.out.println("home!!!!!!!" + false);
        }
        if (ConstantsFor.argNORUNExist("home")) {
            System.out.println("home" + true);
        }
        if (!ConstantsFor.argNORUNExist()) {
            System.out.println(ConstantsFor.class.getCanonicalName() + " !!!!!!!!!!!!!!!!!!!!!");
        }
        if (ConstantsFor.argNORUNExist()) {
            System.out.println(ConstantsFor.class.getCanonicalName());
        }
        if (ConstantsFor.argNORUNExist(ConstantsFor.REGRUHOSTING_PC)) {
            System.out.println("ConstantsFor.REGRUHOSTING_PC" + true);
        }
        if (!ConstantsFor.argNORUNExist(ConstantsFor.REGRUHOSTING_PC)) {
            System.out.println("ConstantsFor.REGRUHOSTING_PC!!!!!!!!!!" + false);
        }
    }

    @Test
    public void testValues() {
        throw new InvokeEmptyMethodException("Values created 10.05.2020 at 12:56");
    }

    @Test
    public void testValueOf() {
        throw new InvokeEmptyMethodException("ValueOf created 10.05.2020 at 12:56");
    }
}