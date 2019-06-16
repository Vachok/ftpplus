// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.springframework.beans.factory.BeanCreationException;
import org.testng.Assert;
import org.testng.annotations.Test;


public class IntoApplicationTest {
    
    
    @Test
    public void testGetConfigurableApplicationContext() {
        try {
            IntoApplication.reloadConfigurableApplicationContext();
        }
        catch (BeanCreationException e) {
            Assert.assertNotNull(e, e.getMessage());
        }
    }
}