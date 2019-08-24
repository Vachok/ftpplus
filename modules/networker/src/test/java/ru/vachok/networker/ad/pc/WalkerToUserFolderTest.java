package ru.vachok.networker.ad.pc;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.net.scanner.NetListsTest;

import java.util.List;


/**
 @see WalkerToUserFolder */
public class WalkerToUserFolderTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
    private WalkerToUserFolder walkerToUserFolder = new WalkerToUserFolder("do0001");
    
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
    public void testCall() {
        String call = walkerToUserFolder.call();
        List<String> userFolderSize = FileSystemWorker.readFileToList(call.split(" ")[0]);
        Assert.assertTrue(userFolderSize.size() > 0, userFolderSize.size() + " userFolderSize");
    }
    
    @Test
    public void testTestToString() {
        throw new InvokeEmptyMethodException("TestToString created 24.08.2019 at 13:04");
    }
}