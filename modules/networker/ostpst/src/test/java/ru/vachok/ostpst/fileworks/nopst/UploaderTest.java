package ru.vachok.ostpst.fileworks.nopst;


import org.testng.annotations.Test;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.fileworks.FileWorker;


public class UploaderTest {
    
    
    @Test(enabled = false)
    public void testUpload() {
        FileWorker fileWorker = new Uploader("c:\\Users\\ikudryashov\\OneDrive\\Документы\\Файлы Outlook\\ksamarchenko.ost", "\\\\192.168.14.10\\IT-Backup\\Mailboxes_users\\ksamarchenko.ost");
        ((Uploader) fileWorker).setBytesBuffer(ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES * 42);
        System.out.println("fileWorker = " + fileWorker.continuousCopy());
    }
}