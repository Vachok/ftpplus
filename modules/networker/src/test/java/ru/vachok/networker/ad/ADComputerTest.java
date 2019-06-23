package ru.vachok.networker.ad;


import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;


/**
 @since 17.06.2019 (10:45) */
public class ADComputerTest {
    
    
    @Test
    public void testToString1() {
        ADComputer adComputer = new ADComputer();
        adComputer.setDnsHostName("do0213.eatmeat.ru");
        assertTrue(adComputer.toString().contains("do0213.eatmeat.ru"));
    }
}