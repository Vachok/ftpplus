// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.PSTException;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.ostpst.utils.CharsetEncoding;

import java.io.File;
import java.io.IOException;


@SuppressWarnings("ALL") public class ParserPSTMessagesTest {
    
    
    @Test(enabled = false)
    public void searchTest() {
        if (!new File("tmp_t.p.magdich.pst").exists()) {
            RNDFileCopy rndFileCopy = new RNDFileCopy("\\\\192.168.14.10\\IT-Backup\\Mailboxes_users\\t.p.magdich.pst");
            String copyStat = rndFileCopy.copyFile("n");
            System.out.println(copyStat);
        }
        ParserPSTMessages pstMessages = new ParserPSTMessages("tmp_t.p.magdich.pst", 32962);
        try {
            System.out.println(pstMessages.searchMessage("Работа SAP (from: Подковыров, Евгений)"));
            System.out.println(pstMessages.searchMessage(2108836));
            Assert.assertTrue(new File("attachments").isDirectory());
        }
        catch (PSTException | IOException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void searchEverywhere() {
        String thingStr = new CharsetEncoding("windows-1251").getStrInAnotherCharset("Входящие");
        String fileName = "c:\\Users\\ikudryashov\\OneDrive\\Документы\\Файлы Outlook\\ksamarchenko.ost";
        try {
            ParserPSTMessages pstMessages = new ParserPSTMessages(fileName, thingStr);
            String searchMessage = pstMessages.searchMessage();
            System.out.println(searchMessage);
        }
        catch (PSTException | IOException e) {
            e.printStackTrace();
        }
    }
}