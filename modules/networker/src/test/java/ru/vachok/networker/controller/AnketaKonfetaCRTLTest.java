package ru.vachok.networker.controller;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.services.AnketaKonfeta;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.testng.Assert.assertTrue;


/**
 @since 14.06.2019 (11:48) */
public class AnketaKonfetaCRTLTest {
    
    
    private final TestConfigureThreadsLogMaker testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
    }
    
    
    @Test
    public void testGetMapForAnketa() {
        HttpServletResponse response = new MockHttpServletResponse();
        HttpServletRequest request = new MockHttpServletRequest();
        Model model = new ExtendedModelMap();
        AnketaKonfetaCRTL anketaKonfetaCRTL = new AnketaKonfetaCRTL(new AnketaKonfeta());
        String mapForAnketa = anketaKonfetaCRTL.getMapForAnketa(request, model);
        assertTrue(mapForAnketa.equals("anketa"));
        assertTrue(model.asMap().size() == 3);
        assertTrue(model.asMap().get("anketahead").toString().contains("IT-"));
    }
    
    @Test
    public void testPostAnketa() {
        HttpServletRequest request = new MockHttpServletRequest();
        Model model = new ExtendedModelMap();
        AnketaKonfeta anketaKonfeta = getAKonfeta();
        AnketaKonfetaCRTL anketaKonfetaCRTL = new AnketaKonfetaCRTL(anketaKonfeta);
        String postAnketa = anketaKonfetaCRTL.postAnketa(anketaKonfeta, model, request);
        assertTrue(postAnketa.equals("ok"));
        AnketaKonfeta konfeta = (AnketaKonfeta) model.asMap().get("anketaKonfeta");
        assertTrue(konfeta.getUserMail().equals("143500@gmail.com"), new TForms().fromArray(model.asMap(), false));
    }
    
    private AnketaKonfeta getAKonfeta() {
        AnketaKonfeta anketaKonfeta = new AnketaKonfeta();
        anketaKonfeta.setQ1Ans("1");
        anketaKonfeta.setQ2Ans("2");
        anketaKonfeta.setAdditionalString("additional");
        anketaKonfeta.setUserMail("143500@gmail.com");
        return anketaKonfeta;
    }
}