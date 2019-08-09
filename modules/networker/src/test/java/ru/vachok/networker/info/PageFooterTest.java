package ru.vachok.networker.info;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 @see PageFooter
 @since 08.08.2019 (10:14) */
public class PageFooterTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(PageFooter.class.getSimpleName(), System.nanoTime());
    
    private static final InformationFactory INFORMATION_FACTORY = new PageFooter();
    
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
    public void testGetInfoAbout() {
        String infoAboutHeader = INFORMATION_FACTORY.getInfoAbout(ModelAttributeNames.ATT_HEAD);
        Assert.assertEquals(infoAboutHeader, "<a href=\"/\">Главная</a>");
        
        String infoAboutFooter = INFORMATION_FACTORY.getInfoAbout(ModelAttributeNames.ATT_FOOTER);
        Assert.assertTrue(infoAboutFooter.contains("<a href=\"/sshacts\">SSH worker (Only Allow Domains)</a><br>"), infoAboutFooter);
        Assert.assertTrue(infoAboutFooter.contains("<p><a href=\"/serviceinfo\"><font color=\"#999eff\">SERVICEINFO</font></a><br>"), infoAboutFooter);
        Assert.assertTrue(infoAboutFooter.contains("<a href=\"/netscan\"><font color=\"#00cc66\">Скан локальных ПК</font></a><br>"), infoAboutFooter);
        Assert.assertTrue(infoAboutFooter.contains("<a href=\"/\"><img align=\"right\" src=\"/images/icons8-плохие-поросята-100g.png\" alt=\"_\"/></a>"), infoAboutFooter);
        
    }
    
    @Test
    public void testSetInfo() {
        INFORMATION_FACTORY.setInfo(FileSystemWorker.readFile("exit.last"));
        Assert.assertTrue(new File(PageFooter.class.getSimpleName() + ".log").exists());
        Assert.assertTrue(new File(PageFooter.class.getSimpleName() + ".log").lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10)));
    }
    
    @Test
    public void testTestToString() {
        String toStr = INFORMATION_FACTORY.toString();
        Assert.assertTrue(toStr.contains("PageFooter["));
    }
}