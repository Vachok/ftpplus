// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;


public class AppComponentsTest {
    
    
    @Test
    public void testGetProps() {
        Properties appProps = AppComponents.getProps();
        Assert.assertTrue(appProps.size() > 10);
    }
}