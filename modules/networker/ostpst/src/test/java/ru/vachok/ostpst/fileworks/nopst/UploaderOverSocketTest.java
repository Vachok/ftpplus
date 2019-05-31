package ru.vachok.ostpst.fileworks.nopst;


import org.testng.annotations.Test;
import ru.vachok.ostpst.fileworks.FileWorker;


public class UploaderOverSocketTest {
    
    
    @Test(enabled = false)
    public void testSocket() {
        FileWorker fileWorker = new UploaderOverSocket();
        fileWorker.continuousCopy();
    }
    
}