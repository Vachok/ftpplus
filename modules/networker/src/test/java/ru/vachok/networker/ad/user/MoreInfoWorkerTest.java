package ru.vachok.networker.ad.user;


import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;


/**
 @since 10.06.2019 (16:05) */
public class MoreInfoWorkerTest {
    
    
    @Test
    public void testSetOnline() {
    }
    
    @Test
    public void testGetUserFromDB() {
        String userFromDB = MoreInfoWorker.getUserFromDB("user: kudr");
        assertTrue(userFromDB.contains("do0213.eatmeat.ru"), userFromDB);
    }
    
    @Test
    public void testGetInfoAbout() {
    }
    
    @Test
    public void testSetInfo() {
    }
}