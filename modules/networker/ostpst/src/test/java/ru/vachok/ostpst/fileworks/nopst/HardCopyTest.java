// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks.nopst;


import org.testng.annotations.Test;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.fileworks.FileWorker;
import ru.vachok.ostpst.utils.FileSystemWorkerOST;


public class HardCopyTest {
    
    
    @Test
    public void hardCPTester() {
        System.setProperty("encoding", "UTF8");
        final long stLong = System.currentTimeMillis();
        FileWorker fileWorker = new HardCopy(FileSystemWorkerOST.getTestPST());
        ((HardCopy) fileWorker).setBufLen((ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES) * 42);
    
        fileWorker.continuousCopy();
        System.out.println(fileWorker.saveAndExit());
    }
}