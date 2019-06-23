package ru.vachok.networker.exe.schedule;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 @since 20.06.2019 (9:41) */
@SuppressWarnings("ALL") public class MailIISLogsCleanerTest {
    
    
    @Test
    public void testRun() {
        MailIISLogsCleaner mailIISLogsCleaner = new MailIISLogsCleaner();
        mailIISLogsCleaner.run();
        Path path = Paths.get("\\\\srv-mail3.eatmeat.ru\\c$\\inetpub\\logs\\LogFiles\\W3SVC1");
        Assert.assertTrue(path.toFile().listFiles().length > 2);
        long filesSize = 0;
        for (File file : path.toFile().listFiles()) {
            filesSize += file.length();
        }
        Assert.assertTrue(ConstantsFor.GBYTE > (filesSize / ConstantsFor.GBYTE), filesSize / ConstantsFor.MBYTE + " IIS logs in megabytes");
    }
}