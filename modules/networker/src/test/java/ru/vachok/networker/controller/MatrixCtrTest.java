// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.MatrixSRV;
import ru.vachok.networker.sysinfo.VersionInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.testng.Assert.*;


/**
 @since 14.06.2019 (14:10) */
public class MatrixCtrTest {
    
    
    @Test
    public void testSetCurrentProvider() {
        VersionInfo versionInfo = new VersionInfo();
        MatrixSRV matrixSRV = new MatrixSRV();
        MatrixCtr matrixCtr = new MatrixCtr(matrixSRV);
        MatrixCtr.setCurrentProvider();
        String currentProvider = matrixCtr.getCurrentProvider();
        assertFalse(currentProvider.isEmpty());
        assertNotNull(matrixCtr.toString());
    }
    
    @Test
    public void testGetFirst() {
        MatrixSRV matrixSRV = new MatrixSRV();
        MatrixCtr matrixCtr = new MatrixCtr(matrixSRV);
        HttpServletRequest httpServletRequest = new MockHttpServletRequest();
        Model model = new ExtendedModelMap();
        HttpServletResponse response = new MockHttpServletResponse();
        
        String matrixCtrFirst = matrixCtr.getFirst(httpServletRequest, model, response);
        assertTrue(matrixCtrFirst.equals("starting"), matrixCtrFirst + " is wrong!");
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
        matrixCtr.getWorkPosition(matrixSRV, model);
    }
    
    @Test
    public void testGitOn() {
        MatrixSRV matrixSRV = new MatrixSRV();
        MatrixCtr matrixCtr = new MatrixCtr(matrixSRV);
        Model model = new ExtendedModelMap();
        HttpServletRequest httpServletRequest = new MockHttpServletRequest();
        
        String gitOnStr = matrixCtr.gitOn(model, httpServletRequest);
        assertTrue(gitOnStr.equals("redirect:http://srv-git.eatmeat.ru:1234"));
        assertTrue(model.asMap().get("head").toString().contains("Главная"));
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
            assertTrue(model.asMap().get("workPos").toString().equals("whois: ya.ru"));
        }
        catch (IOException e) {
            assertNull(e, e.getMessage());
        }
    }
}