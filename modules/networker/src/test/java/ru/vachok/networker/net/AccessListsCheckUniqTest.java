// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 @see AccessListsCheckUniq
 @since 30.07.2019 (11:21) */
@SuppressWarnings("ThrowCaughtLocally")
public class AccessListsCheckUniqTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private boolean isHome = getIsHome();
    
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
            new AccessListsCheckUniq().run();
            File file = new File(FileNames.FILENAME_INETUNIQ);
            Assert.assertTrue(file.exists());
            Assert.assertTrue(file.lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10)));
        }
    }
    
    @Test
    public void testConnectTo() {
        if (!isHome) {
            String connectToResult = new AccessListsCheckUniq().connectTo();
            Assert.assertTrue(connectToResult.contains("####### SQUID FULL ##########"), connectToResult);
        }
    
    }
    
    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    private boolean getIsHome() {
        boolean isHome = ConstantsFor.thisPC().toLowerCase().contains("home");
        if (isHome) {
            try {
                throw new InvokeIllegalException();
                
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