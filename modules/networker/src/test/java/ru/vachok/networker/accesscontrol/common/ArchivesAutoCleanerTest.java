package ru.vachok.networker.accesscontrol.common;


import org.testng.annotations.Test;


/**
 @since 17.06.2019 (10:51) */
@SuppressWarnings("ALL") public class ArchivesAutoCleanerTest {
    
    
    @Test(enabled = false)
    public void testRun() {
        ArchivesAutoCleaner autoCleaner = new ArchivesAutoCleaner();
        autoCleaner.toString();
        autoCleaner.run();
    }
}