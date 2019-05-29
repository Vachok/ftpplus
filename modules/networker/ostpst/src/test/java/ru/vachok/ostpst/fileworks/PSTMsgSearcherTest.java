// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.PSTException;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.utils.CharsetEncoding;
import ru.vachok.ostpst.utils.FileSystemWorkerOST;
import ru.vachok.ostpst.utils.TFormsOST;

import java.io.File;
import java.io.IOException;


public class PSTMsgSearcherTest {
    
    
    @Test
    public void searchTest() {
        System.setProperty(ConstantsOst.STR_ENCODING, "UTF8");
        PSTMsgSearcher pstMsgSearcher = new PSTMsgSearcher(ConstantsOst.FILENAME_TESTPST, 32962);
        try {
            System.out.println(pstMsgSearcher.searchMessage(new CharsetEncoding(ConstantsOst.CP_WINDOWS_1251, "UTF-8").getStrInAnotherCharset("Работа SAP (from: Подковыров, Евгений)")));
            System.out.println(pstMsgSearcher.searchMessage(2108836));
            Assert.assertTrue(new File(ConstantsOst.STR_ATTACHMENTS).isDirectory());
        }
        catch (PSTException | IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    @Test(enabled = false)
    public void searchEverywhere() {
        String thingStr = new CharsetEncoding(ConstantsOst.CP_WINDOWS_1251).getStrInAnotherCharset("fw:");
        String fileName = ConstantsOst.FILENAME_TESTPST;
        try {
            PSTMsgSearcher pstMessages = new PSTMsgSearcher(fileName, thingStr);
            String searchMessage = pstMessages.everywhereSearch();
            System.out.println(searchMessage);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TFormsOST().fromArray(e));
        }
    }
    
    @Test
    public void searchEmails() {
        PSTMsgSearcher pstMsgSearcher = new PSTMsgSearcher(FileSystemWorkerOST.getTestPST(), "143500@gmail.com");
        System.out.println(pstMsgSearcher.byEmail());
    }
}