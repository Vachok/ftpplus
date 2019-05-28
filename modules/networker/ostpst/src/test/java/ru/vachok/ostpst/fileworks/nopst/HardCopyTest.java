package ru.vachok.ostpst.fileworks.nopst;


import org.testng.annotations.Test;
import ru.vachok.ostpst.fileworks.FileWorker;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class HardCopyTest {
    
    
    @Test
    public void hardCPTester() {
        final long stLong = System.currentTimeMillis();
        FileWorker fileWorker = new HardCopy("\\\\192.168.14.10\\IT-Backup\\Mailboxes_users\\oratnikova.pst", "orat.pst");
        Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(fileWorker::continuousCopy);
        while ((stLong + TimeUnit.SECONDS.toMillis(15) > System.currentTimeMillis())) {
            fileWorker.showCurrentResult();
            try {
                Thread.sleep(5000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}