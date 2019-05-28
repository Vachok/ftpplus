// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks.nopst;


import org.testng.annotations.Test;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.fileworks.FileWorker;


public class HardCopyTest {
    
    
    @Test
    public void hardCPTester() {
        final long stLong = System.currentTimeMillis();
        FileWorker fileWorker = new HardCopy("\\\\192.168.14.10\\IT-Backup\\Mailboxes_users\\a.v.komarov.pst", "a.v.komarov.pst");
        ((HardCopy) fileWorker).setBufLen((ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES) * 42);
    
        fileWorker.continuousCopy();
        System.out.println(fileWorker.saveAndExit());
    }
}