// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.annotations.Test;


public class IntoApplicationTest {
    
    
    @Test()
    public void testGetConfigurableApplicationContext() {
        IntoApplication.reloadConfigurableApplicationContext();
    }
}