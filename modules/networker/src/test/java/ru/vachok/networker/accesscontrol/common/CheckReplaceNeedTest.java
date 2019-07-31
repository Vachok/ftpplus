package ru.vachok.networker.accesscontrol.common;


import org.testng.annotations.Test;


/**
 @see CheckReplaceNeed
 @since 31.07.2019 (10:08) */
public class CheckReplaceNeedTest {
    
    
    private Runnable chkRep = new CheckReplaceNeed();
    
    @Test(enabled = false)
    public void testRun() {
        chkRep.run();
    }
    
    @Test
    public void testTestToString() {
        System.out.println("chkRep.toString() = " + chkRep.toString());
    }
}