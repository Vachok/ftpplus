package ru.vachok.networker.accesscontrol.common;


import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;


/**
 @since 17.06.2019 (15:02) */
public class CommonSRVTest {
    
    
    @Test
    public void testSearchByPat() {
        String searchInCommonResult = new CommonSRV().searchByPat("График отпусков:14_ИТ_служба\\Общая");
        String searchInCommonResult1 = new CommonSRV().searchByPat(":");
        assertTrue(searchInCommonResult.contains("График отпусков 2019г  IT.XLSX"), searchInCommonResult);
        assertTrue(searchInCommonResult.contains("График отпусков 2019г. SA.xlsx"), searchInCommonResult);
        assertTrue(searchInCommonResult1.contains("Bytes in stream:"));
    }
    
    @Test
    public void testReStoreDir() {
        String reStoreDirResult = new CommonSRV().reStoreDir();
        assertTrue(reStoreDirResult.contains("TERMINATE"));
    }
}