// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.HTMLInfo;
import ru.vachok.networker.net.scanner.NetListsTest;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.text.MessageFormat;
import java.util.concurrent.RejectedExecutionException;


/**
 @see PCUserNameHTMLResolver
 @since 16.08.2019 (20:37) */
public class PCUserNameHTMLResolverTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
    private String pcName = "do0001";
    
    private HTMLInfo htmlInfo = new PCUserNameHTMLResolver(pcName);
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
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
    public void testFillWebModel() {
        htmlInfo.setClassOption(pcName);
        String inModelStr = htmlInfo.fillWebModel();
        System.out.println("inModelStr = " + inModelStr);
    }
    
    @Test
    public void testFillAttribute() {
        String infoAboutName = "null";
        try {
            infoAboutName = htmlInfo.fillAttribute(pcName);
        }
        catch (RejectedExecutionException e) {
            messageToUser.error(MessageFormat
                .format("PCUserNameResolverTest.testGetInfo {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        Assert.assertTrue(infoAboutName.contains("/200 GET"), infoAboutName);
        this.pcName = "10.200.213.85";
        String infoAboutIP = htmlInfo.fillAttribute(pcName);
        Assert.assertTrue(infoAboutIP.contains("a href"), infoAboutIP);
    }
    
    @Test
    public void testTestToString() {
        String toStr = htmlInfo.toString();
        Assert.assertTrue(toStr.contains("PCUserNameHTMLResolver["), toStr);
    }
}