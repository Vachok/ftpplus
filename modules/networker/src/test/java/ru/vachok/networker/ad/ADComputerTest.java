// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad;


import org.testng.annotations.Test;
import ru.vachok.networker.net.enums.OtherKnownDevices;

import static org.testng.Assert.assertTrue;


/**
 @since 17.06.2019 (10:45) */
public class ADComputerTest {
    
    
    @Test
    public void testToString1() {
        ADComputer adComputer = new ADComputer();
        adComputer.setDnsHostName(OtherKnownDevices.DO0213_KUDR);
        assertTrue(adComputer.toString().contains(OtherKnownDevices.DO0213_KUDR));
    }
}