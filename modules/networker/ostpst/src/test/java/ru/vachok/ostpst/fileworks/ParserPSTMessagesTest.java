// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.PSTException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;


@SuppressWarnings("ALL") public class ParserPSTMessagesTest {
    
    
    @Test
    public void msgSubjectsTest() {
        try {
            ParserPSTMessages pstMessages = new ParserPSTMessages("G:\\My_Proj\\FtpClientPlus\\modules\\networker\\ostpst\\tmp_t.p.magdich.pst", 32962);
            Map<Long, String> subjects = pstMessages.getMessagesSubject();
            Assert.assertTrue(subjects.size() > 0);
        }
        catch (PSTException | IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
}