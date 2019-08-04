package ru.vachok.networker.controller;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.services.SimpleCalculator;

import javax.servlet.http.HttpServletRequest;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


/**
 @since 14.06.2019 (12:21) */
public class CalculateCTRLTest {
    
    
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
    public void testGetM() {
        CalculateCTRL calculateCTRL = new CalculateCTRL(new SimpleCalculator());
        Model model = new ExtendedModelMap();
        HttpServletRequest request = new MockHttpServletRequest();
        String calculateCTRLM = calculateCTRL.getM(model, request);
        assertTrue(calculateCTRLM.equals("calculate"));
        assertTrue(model.asMap().size() == 3);
        SimpleCalculator simpleCalculator = (SimpleCalculator) model.asMap().get("simpleCalculator");
        assertTrue(simpleCalculator instanceof SimpleCalculator);
        
    }
    
    @Test
    public void testTimeStamp() {
        SimpleCalculator simpleCalculator = new SimpleCalculator();
        Model model = new ExtendedModelMap();
        CalculateCTRL calculateCTRL = new CalculateCTRL(simpleCalculator);
        String timeStamp = calculateCTRL.timeStamp(simpleCalculator, model, "t:1");
        assertFalse(timeStamp.isEmpty());
        assertTrue(model.asMap().get("result").toString().equals("Thu Jan 01 03:00:00 MSK 1970"));
    }
}