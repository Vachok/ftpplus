// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.util.concurrent.*;


/**
 @see PCOn
 @since 23.06.2019 (15:11) */
@SuppressWarnings("ALL")
public class PCOnTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private PCOn pcInfo = new PCOn("do0045");
    
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
    public void testToString() {
        String toStr = pcInfo.toString();
        Assert.assertTrue(toStr.contains("PCOn["), toStr);
        pcInfo.setClassOption("pp0001");
        toStr = pcInfo.toString();
        Assert.assertTrue(toStr.contains("pcName = 'pp0001'"), toStr);
    }
    
    @Test
    public void testGetInfo() {
        String info = pcInfo.getInfo();
        Assert.assertTrue(info.contains("online true"), info);
    }
    
    /**
     Отдаёт HTML-строку вида:
     Крайнее имя пользователя на ПК do0045.eatmeat.ru - \\do0045.eatmeat.ru\c$\Users\kpivovarov<br>
     ( Thu May 31 09:34:39 MSK 2018 )<p>
     Список всех зарегистрированных пользователей ПК:<br>
     \\do0045.eatmeat.ru\c$\Users\kpivovarov Thu May 31 09:34:39 MSK 2018<br>
     \\do0045.eatmeat.ru\c$\Users\desktop.ini Tue Jul 14 08:41:57 MSD 2009<br>
     */
    @Test
    public void testGetInfoAbout() {
        String infoAbout = pcInfo.getInfoAbout("do0045");
        Assert.assertTrue(infoAbout.contains("kpivovarov"), infoAbout);
        Assert.assertTrue(infoAbout.startsWith("Крайнее имя пользователя на ПК do0045 - kpivovarov"), infoAbout);
    }
    
    @Test
    public void incorrectIntegerValueDO0058() {
        this.pcInfo = new PCOn("do0058");
        Future<String> infoFuture = AppComponents.threadConfig().getTaskExecutor().submit(()->pcInfo.getInfo());
        String info = "";
        String do0058 = "pcInfo.getInfoAbout(do0058)";
        Future<String> do0058Future = AppComponents.threadConfig().getTaskExecutor().submit(()->pcInfo.getInfoAbout("do0058"));
        try {
            info = infoFuture.get(30, TimeUnit.SECONDS);
            do0058 = do0058Future.get(30, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        
        Assert.assertTrue(info.contains("<br><b><a href=\"/ad?do0058\">do0058</a>"), do0058);
        Assert.assertTrue(do0058.toLowerCase().contains("dpetrov"), do0058);
    }
    
    @Test
    public void testAfterDO0086() {
        this.pcInfo = new PCOn("do0087");
        String infoAbout = pcInfo.getInfoAbout("do0088");
        String info = pcInfo.getInfo();
        Assert.assertTrue(info.contains("unknown pc: do0088.eatmeat.ru"), info);
        Assert.assertTrue(infoAbout.contains("Крайнее имя пользователя на ПК unknown pc: do0088.eatmeat.ru"), infoAbout);
    }
}