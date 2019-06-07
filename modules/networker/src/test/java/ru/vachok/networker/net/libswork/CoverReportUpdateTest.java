package ru.vachok.networker.net.libswork;


import org.testng.annotations.Test;


public class CoverReportUpdateTest {
    
    
    @Test(enabled = false)
    public void uploadCover() {
        CoverReportUpdate coverReportUpdate = new CoverReportUpdate();
        coverReportUpdate.run();
    }
    
    @Test
    public void testCall() {
        CoverReportUpdate coverReportUpdate = new CoverReportUpdate();
        try {
            System.out.println(coverReportUpdate.call());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}