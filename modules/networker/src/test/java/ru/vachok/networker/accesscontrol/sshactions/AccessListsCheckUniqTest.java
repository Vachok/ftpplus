// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.sshactions;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.util.concurrent.*;


/**
 @see AccessListsCheckUniq
 @since 30.07.2019 (11:21) */
@SuppressWarnings("ThrowCaughtLocally")
public class AccessListsCheckUniqTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private boolean isHome = getIsHome();
    
    private AccessListsCheckUniq accessListsCheckUniq = new AccessListsCheckUniq();
    
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
    public void testRun() {
        if (!isHome) {
            Future<String> stringFuture = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(accessListsCheckUniq);
            try {
                String uniqStr = stringFuture.get(30, TimeUnit.SECONDS);
                Assert.assertFalse(uniqStr.isEmpty());
            }
            catch (InterruptedException | ExecutionException | TimeoutException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
            File file = new File(FileNames.FILENAME_INETUNIQ);
            Assert.assertTrue(file.exists());
            Assert.assertTrue(file.lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10)));
        }
    }
    
    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    private boolean getIsHome() {
        boolean isHome = UsefulUtilities.thisPC().toLowerCase().contains("home");
        if (isHome) {
            try {
                throw new InvokeIllegalException("Not running at home PC");
                
            }
            catch (InvokeIllegalException e) {
                Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
        }
        return isHome;
    }
    
    @Test
    public void testTestToString() {
        String toStr = new AccessListsCheckUniq().toString();
        Assert.assertTrue(toStr.contains("AccessListsCheckUniq["), toStr);
    }
}