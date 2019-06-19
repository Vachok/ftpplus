package ru.vachok.ostpst.fileworks.nopst;


import org.testng.annotations.Test;
import ru.vachok.ostpst.api.FileProperties;
import ru.vachok.ostpst.api.InitProperties;
import ru.vachok.ostpst.fileworks.FileWorker;


public class UploaderOverSocketTest {
    
    
    @Test(enabled = false)
    public void testSocket() {
        InitProperties initProperties = new FileProperties("ostpst.properties");
        FileWorker fileWorker = new UploaderOverSocket(initProperties);
        fileWorker.continuousCopy();
    }
    
}