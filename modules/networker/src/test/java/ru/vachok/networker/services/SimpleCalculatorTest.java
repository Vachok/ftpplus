// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;


/**
 @since 21.07.2019 (19:57) */
public class SimpleCalculatorTest {
    
    
    @Test
    public void testGetQuery() {
        String queryString = new SimpleCalculator().getQuery();
        Assert.assertNull(queryString);
    }
    
    @Test
    public void testSetQuery() {
        SimpleCalculator calculator = new SimpleCalculator();
        calculator.setQuery("test");
        Assert.assertEquals(calculator.getQuery(), "test");
    }
    
    @Test
    public void testGetStampFromDate() {
        SimpleCalculator calculator = new SimpleCalculator();
        String date = calculator.getStampFromDate("07-01-1984-20-06");
        Assert.assertEquals(Long.parseLong(date), 442343160000L);
    }
    
    @Test
    public void testCountDoubles() {
        SimpleCalculator calculator = new SimpleCalculator();
        List<Double> doubleList = new ArrayList<>();
        doubleList.add(2.0);
        doubleList.add(2.0);
        double v = calculator.countDoubles(doubleList);
        Assert.assertTrue(v == 4);
    }
    
    @Test
    public void testToString1() {
        String toStr = new SimpleCalculator().toString();
        Assert.assertTrue(toStr.contains("SimpleCalculator{"));
    }
}