package ru.vachok.networker.accesscontrol;


import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;


/**
 @see MatrixSRV
 @since 21.06.2019 (12:40) */
public class MatrixSRVTest {
    
    
    /**
     @see MatrixSRV#searchAccessPrincipals(String)
     */
    @Test
    public void testSearchAccessPrincipals() {
        MatrixSRV matrixSRV = new MatrixSRV();
        String accessPrincipals = matrixSRV.searchAccessPrincipals("адми");
        assertTrue(accessPrincipals.contains("администр"));
    }
}