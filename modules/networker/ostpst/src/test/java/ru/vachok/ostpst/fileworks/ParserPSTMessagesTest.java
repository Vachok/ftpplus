// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.PSTException;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.utils.CharsetEncoding;
import ru.vachok.ostpst.utils.TFormsOST;

import java.io.File;
import java.io.IOException;


@SuppressWarnings("ALL") public class ParserPSTMessagesTest {
    
    
    @Test()
    public void searchTest() {
        if (!new File(ConstantsOst.FILENAME_TESTPST).exists()) {
            RNDFileCopy rndFileCopy = new RNDFileCopy("\\\\192.168.14.10\\IT-Backup\\Mailboxes_users\\t.p.magdich.pst");
            String copyStat = rndFileCopy.copyFile("n");
            System.out.println(copyStat);
        }
        PSTMsgSearcher pstMsgSearcher = new PSTMsgSearcher(ConstantsOst.FILENAME_TESTPST, 32962);
        try {
            System.out.println(pstMsgSearcher.searchMessage("Работа SAP (from: Подковыров, Евгений)"));
            System.out.println(pstMsgSearcher.searchMessage(2108836));
            Assert.assertTrue(new File(ConstantsOst.STR_ATTACHMENTS).isDirectory());
        }
        catch (PSTException | IOException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void searchEverywhere() {
        String thingStr = new CharsetEncoding("windows-1251").getStrInAnotherCharset("fw:");
        String fileName = "\\\\192.168.14.10\\IT-Backup\\Mailboxes_users\\t.p.magdich.pst";
        try {
            PSTMsgSearcher pstMessages = new PSTMsgSearcher(fileName, thingStr);
            String searchMessage = pstMessages.everywhereSearch();
            System.out.println(searchMessage);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TFormsOST().fromArray(e));
        }
    }
}