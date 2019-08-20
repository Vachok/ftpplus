// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLInfo;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.PCInfo;
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
    
    private InformationFactory informationFactory = new PCUserNameHTMLResolver(pcName);
    
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
    
    @Test
    public void testGetUserByPC() {
        String kudr = ((PCInfo) informationFactory).getUserByPC("do0213");
        Assert.assertEquals(kudr, "ikudryashov");
    }
    
    @Test
    public void testGetPCbyUser() {
        this.informationFactory = InformationFactory.getInstance(InformationFactory.SEARCH_PC_IN_DB);
        String do0213 = ((PCInfo) informationFactory).getPCbyUser("kudr");
        Assert.assertTrue(do0213.contains("ikudryashov"));
        Assert.assertFalse(do0213.contains("eatmeat.ru.eatmeat.ru"));
    }
    
    @Test
    public void testGetInfoAbout() {
        String infoAbout = informationFactory.getInfoAbout(pcName);
        System.out.println("infoAbout = " + infoAbout);
    }
    
    @Test
    public void testTestToString1() {
        throw new InvokeEmptyMethodException("20.08.2019 (19:45)");
    }
    
    @Test
    public void testSetClassOption() {
        throw new InvokeEmptyMethodException("20.08.2019 (19:45)");
    }
    
    @Test
    public void testGetInfo() {
        throw new InvokeEmptyMethodException("20.08.2019 (19:45)");
    }
}