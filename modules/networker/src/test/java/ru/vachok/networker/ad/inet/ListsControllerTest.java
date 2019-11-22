package ru.vachok.networker.ad.inet;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;

import java.util.Random;
import java.util.stream.Stream;


/**
 @see ListsController
 @since 20.11.2019 (14:10) */
public class ListsControllerTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(ListsControllerTest.class.getSimpleName(), System.nanoTime());
    
    private ListsController listsController;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @BeforeMethod
    public void setInetUse() {
        this.listsController = (ListsController) InformationFactory.getInstance(InformationFactory.LISTS_CONTROLLER);
    }
    
    @Test
    public void testGetInfo() {
        String listsControllerInfo = listsController.getInfo();
        Assert.assertTrue(listsControllerInfo.contains("vipnet"), listsControllerInfo);
    }
    
    @Test
    public void testGetInfoAbout() {
        String[] fileNames = listsController.getInfo().split("\n");
        for (int i = 0; i < 3; i++) {
            String fileName = fileNames[new Random().nextInt(fileNames.length)];
            String listsControllerInfoAbout = listsController.getInfoAbout("sudo cat /etc/pf/" + fileName + ";exit");
            Assert.assertTrue(Stream
                    .of("10.", "212.45.3.116", "", "mail.yandex", "93.158.134.0/24", "81.222.128.0/24", "windowsupdate", "8.8.8.8", "image/jpeg", "gostexpert", "Istranet", "192.", "yandsearch", "46.17.203.51/16")
                    .anyMatch(listsControllerInfoAbout::contains), fileName + "\n" + listsControllerInfoAbout);
        }
    }
    
    @Test
    public void testToString() {
        String toString = listsController.toString();
        Assert.assertTrue(toString.contains("ListsController{"), toString);
    }
}