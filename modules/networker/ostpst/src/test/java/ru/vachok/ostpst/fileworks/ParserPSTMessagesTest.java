// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.PSTException;
import org.testng.annotations.Test;

import java.io.IOException;


public class ParserPSTMessagesTest {
    
    
    @Test(enabled = false)
    public void searchTest() {
        ParserPSTMessages pstMessages = new ParserPSTMessages("\\\\192.168.14.10\\IT-Backup\\Mailboxes_users\\t.p.magdich.pst", 32962);
        try {
            System.out.println(pstMessages.searchBySubj("новый"));
        }
        catch (PSTException | IOException e) {
            e.printStackTrace();
        }
    }
}