// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.net.scanner.ScanOnline;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 @see ShowAllDevCTRL
 @since 20.07.2019 (10:14) */
public class ShowAllDevCTRLTest {
    
    
    @Test(invocationCount = 5)
    public void testModel() {
        Model model = new ExtendedModelMap();
        HttpServletResponse response = new MockHttpServletResponse();
        HttpServletRequest request = new MockHttpServletRequest();
        String modelMake = new ShowAllDevCTRL(new ScanOnline()).allDevices(model, request, response);
        Assert.assertTrue(modelMake.equals("ok"));
    }
    
}