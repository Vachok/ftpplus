package ru.vachok.networker.info;


import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see DatabaseInfo
 @since 16.08.2019 (10:43) */
public class DatabaseInfoTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(DatabaseInfoTest.class.getSimpleName(), System
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
    public void testGetUserPCFromDB() {
    }
    
    @Test
    public void testGetPCUsersFromDB() {
    }
    
    @Test
    public void testGetInfo() {
        InformationFactory informationUser = InformationFactory.getInstance("kudr");
        checkFactory(informationUser);
        InformationFactory informationPC = InformationFactory.getInstance("do0213");
        checkFactory(informationPC);
    }
    
    private void checkFactory(InformationFactory factory) {
        String getInfoToString = factory.toString();
        String factoryInfo = factory.getInfo();
        System.out.println("getInfoToString = " + getInfoToString);
        System.out.println("factoryInfo = " + factoryInfo);
    }
}