// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import org.testng.annotations.Test;


public class PCStatsTest {
    
    
    @Test
    public void getSt() {
        System.out.println(new PCStats().call());
    }
    
}