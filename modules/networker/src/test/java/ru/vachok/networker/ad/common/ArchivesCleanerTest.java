package ru.vachok.networker.ad.common;


import org.junit.Test;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;


public class ArchivesCleanerTest {
    
    
    @Test
    public void cleanArch() {
        AppConfigurationLocal.getInstance().execute(new ArchivesCleaner(), 30);
    }
}