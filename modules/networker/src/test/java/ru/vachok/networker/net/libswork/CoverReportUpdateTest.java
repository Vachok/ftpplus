// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.libswork;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;


@SuppressWarnings("ALL") public class CoverReportUpdateTest {
    
    
    @Test(enabled = false)
    public void uploadCover() {
        CoverReportUpdate coverReportUpdate = new CoverReportUpdate();
        coverReportUpdate.run();
    }
    
    @Test(enabled = false)
    public void testCall() {
        CoverReportUpdate coverReportUpdate = new CoverReportUpdate();
        try {
            String callToUpload = coverReportUpdate.call();
            Assert.assertFalse(callToUpload.contains("Exception: "), callToUpload);
            Assert.assertFalse(callToUpload.contains("Connection reset by peer"), callToUpload);
            Assert.assertFalse(callToUpload.contains("socket write error"), callToUpload);
            Assert.assertTrue(callToUpload.contains("226 Transfer complete"), callToUpload);
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
}