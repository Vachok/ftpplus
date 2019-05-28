// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks.nopst;


import org.testng.annotations.Test;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.fileworks.FileWorker;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class HardCopyTest {
    
    
    @Test
    public void hardCPTester() {
        final long stLong = System.currentTimeMillis();
        FileWorker fileWorker = new HardCopy("\\\\192.168.14.10\\IT-Backup\\Mailboxes_users\\oratnikova.pst", "orat.pst");
        Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(fileWorker::continuousCopy);
        while ((stLong + TimeUnit.SECONDS.toMillis(16) > System.currentTimeMillis())) {
            ((HardCopy) fileWorker).setBufLen((ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES) * 30);
            fileWorker.showCurrentResult();
            try {
                Thread.sleep(4000);
            }
            catch (InterruptedException e) {
                fileWorker.saveAndExit();
            }
        }
        System.out.println(fileWorker.saveAndExit());
    }
}