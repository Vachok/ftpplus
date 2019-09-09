// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.info.InformationFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.testng.Assert.*;


/**
 @see MatrixCtr
 @since 14.06.2019 (14:10) */
public class MatrixCtrTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
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
    public void testGetFirst() {
        MatrixSRV matrixSRV = new MatrixSRV();
        MatrixCtr matrixCtr = new MatrixCtr(matrixSRV);
        HttpServletRequest httpServletRequest = new MockHttpServletRequest();
        Model model = new ExtendedModelMap();
        HttpServletResponse response = new MockHttpServletResponse();
        
        String matrixCtrFirst = matrixCtr.getFirst(httpServletRequest, model, response);
        assertTrue(matrixCtrFirst.equals(ConstantsFor.STARTING), matrixCtrFirst + " is wrong!");
        assertTrue(response.getHeader("Refresh").equals("120"), new TForms().fromArray(response.getHeaders("Refresh"), false));
    }
    
    @Test
    public void testGetWorkPosition() {
        MatrixSRV matrixSRV = new MatrixSRV();
        MatrixCtr matrixCtr = new MatrixCtr(matrixSRV);
        HttpServletRequest httpServletRequest = new MockHttpServletRequest();
        Model model = new ExtendedModelMap();
        HttpServletResponse response = new MockHttpServletResponse();
        
        matrixSRV.setWorkPos("адми");
        String matrixCtrWorkPosition = matrixCtr.getWorkPosition(matrixSRV, model);
        assertEquals(matrixCtrWorkPosition, "ok");
        assertTrue(model.asMap().size() >= 1);
        assertTrue(model.asMap().get("ok").toString().contains("адми"));
    }
    
    @Test
    public void testShowResults() {
        MatrixSRV matrixSRV = new MatrixSRV();
        MatrixCtr matrixCtr = new MatrixCtr(matrixSRV);
        HttpServletRequest httpServletRequest = new MockHttpServletRequest();
        Model model = new ExtendedModelMap();
        HttpServletResponse response = new MockHttpServletResponse();
        
        try {
            String showResultsStr = matrixCtr.showResults(httpServletRequest, response, model);
            assertTrue(showResultsStr.equals(ConstantsFor.BEANNAME_MATRIX));
            assertTrue(response.getStatus() == 200);
            assertTrue(showResultsStr.equals("matrix"));
            assertTrue(model.asMap().get(ModelAttributeNames.WORKPOS).toString().equals("whois: ya.ru"));
        }
        catch (IOException e) {
            assertNull(e, e.getMessage());
        }
    }
    
    @Test
    public void ptvString() {
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.TV);
        String info = informationFactory.getInfo();
        Assert.assertTrue(info.contains("on ptv1.eatmeat.ru"), info);
        Assert.assertTrue(info.contains("on ptv2.eatmeat.ru"), info);
    }
}