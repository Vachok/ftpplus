// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
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

    private MatrixCtr matrixCtr;

    private MatrixSRV matrixSRV;

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }

    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }

    @BeforeMethod
    public void initMatrix() {
        this.matrixSRV = new MatrixSRV();
        this.matrixCtr = new MatrixCtr(matrixSRV);
    }

    @Test
    public void testGetFirst() {
        HttpServletRequest httpServletRequest = new MockHttpServletRequest();
        Model model = new ExtendedModelMap();
        HttpServletResponse response = new MockHttpServletResponse();

        String matrixCtrFirst = matrixCtr.getFirst(httpServletRequest, model, response);
        assertTrue(matrixCtrFirst.equals(ConstantsFor.STARTING), matrixCtrFirst + " is wrong!");
        assertTrue(response.getHeader(ConstantsFor.HEAD_REFRESH).equals("120"), AbstractForms.fromArray(response.getHeaders(ConstantsFor.HEAD_REFRESH)));
    }

    @Test
    public void testGetWorkPosition() {
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
    public void testParseLong() {
        Model model = new ExtendedModelMap();
        String matrixCtrWorkPosition;
        matrixSRV.setWorkPos(String.valueOf(System.currentTimeMillis()));
        matrixCtrWorkPosition = matrixCtr.getWorkPosition(matrixSRV, model);
        System.out.println(matrixCtrWorkPosition + " = " + AbstractForms.fromArray(model.asMap()));
    }

    @Test
    public void testShowResults() {
        HttpServletRequest httpServletRequest = new MockHttpServletRequest();
        Model model = new ExtendedModelMap();
        HttpServletResponse response = new MockHttpServletResponse();

        try {
            String showResultsStr = matrixCtr.showResults(httpServletRequest, response, model);
            assertTrue(showResultsStr.equals(ConstantsFor.BEANNAME_MATRIX));
            assertTrue(response.getStatus() == 200);
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

    @Test
    public void testSetMailIsOk() {
        String isOkStr = MatrixCtr.setMailIsOk("test");
        Assert.assertEquals(isOkStr, "test");
    }

    @Test
    public void testTestToString() {
        String s = matrixCtr.toString();
        Assert.assertTrue(s.contains("MatrixCtr{"), s);
    }
}