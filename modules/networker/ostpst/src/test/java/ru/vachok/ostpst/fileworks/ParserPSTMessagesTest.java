// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.PSTException;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.utils.TFormsOST;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;


@SuppressWarnings("ALL") public class ParserPSTMessagesTest {
    
    
    @Test(enabled = false)
    public void searchTest() {
        if (!new File(ConstantsOst.FILENAME_TESTPST).exists()) {
            RNDFileCopy rndFileCopy = new RNDFileCopy("\\\\192.168.14.10\\IT-Backup\\Mailboxes_users\\t.p.magdich.pst");
            String copyStat = rndFileCopy.copyFile("n");
            System.out.println(copyStat);
        }
        ParserPSTMessages pstMessages = new ParserPSTMessages(ConstantsOst.FILENAME_TESTPST, 32962);
        try {
            System.out.println(pstMessages.searchMessage("Работа SAP (from: Подковыров, Евгений)"));
            System.out.println(pstMessages.searchMessage(2108836));
            Assert.assertTrue(new File(ConstantsOst.STR_ATTACHMENTS).isDirectory());
        }
        catch (PSTException | IOException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void searchEverywhere() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        final long start = System.currentTimeMillis();
        String thingStr = "еее";
        String fileName = "G:\\My_Proj\\FtpClientPlus\\modules\\networker\\ostpst\\tmp_t.p.magdich.pst";
        try {
            ParserPSTMessages pstMessages = new ParserPSTMessages(fileName, thingStr);
            String searchMessage = pstMessages.searchMessage();
            System.out.println(searchMessage);
            final long stop = System.currentTimeMillis();
        }
        catch (PSTException | IOException | ArrayIndexOutOfBoundsException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TFormsOST().fromArray(e));
        }
    }
}