// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.net.scanner.ScanOnline;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 @see ShowAllDevCTRL
 @since 20.07.2019 (10:14) */
public class ShowAllDevCTRLTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(ShowAllDevCTRLTest.class.getSimpleName(), System
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
    public void testModel() {
        Model model = new ExtendedModelMap();
        HttpServletResponse response = new MockHttpServletResponse();
        HttpServletRequest request = new MockHttpServletRequest();
        String modelMake = new ShowAllDevCTRL(new ScanOnline()).allDevices(model, request, response);
        Assert.assertTrue(model.asMap().get("title").toString().contains(ConstantsFor.STR_IPREMAIN));
        Assert.assertEquals(model.asMap().get("footer"), "<a href=\"/\"><img align=\"right\" src=\"/images/icons8-плохие-поросята-100g.png\" alt=\"_\"/></a>\n" +
            "<a href=\"/pflists\"><font color=\"#00cc66\">Списки PF</font></a><br>\n" +
            "<a href=\"/netscan\"><font color=\"#00cc66\">Скан локальных ПК</font></a><br>\n" +
            "<a href=\"/odinass\">Сформировать лист команд PoShell для сверки должностей</a><br>\n" +
            "<a href=\"/exchange\"><strike>Парсинг правил MS Exchange</a><br></strike>\n" +
            "<a href=\"/adphoto\">Добавить фотографии в Outlook</a><br>\n" +
            "<a href=\"/common\"><font color=\"#00cc66\">Восстановить из архива</font></a><br>\n" +
            "<a href=\"/sshacts\">SSH worker (Only Allow Domains)</a><br>\n" +
            "<p><a href=\"/serviceinfo\"><font color=\"#999eff\">SERVICEINFO</font></a><br>\n" +
            "<font size=\"1\"><p align=\"right\">By Vachok. (c) 2019</font></p>. Left: 15045 IPs.");
        Assert.assertEquals(model.asMap().get("head"), "<center><font color=\"\"><a href=\"/\">Главная</a>\n" +
                "</font></center><center><p><a href=\"/showalldev?needsopen\"><h2>Show All IPs in file</h2></a></center>");
        String pcsAttribute = model.asMap().get("pcs").toString();
        Assert.assertTrue(pcsAttribute.contains("MSK 1970 last ExecScan: "), pcsAttribute);
        String okAttribute = model.asMap().get("ok").toString();
        Assert.assertFalse(okAttribute.isEmpty(), okAttribute);

        Assert.assertTrue(modelMake.equals("ok"));
    }

}